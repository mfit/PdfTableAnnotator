package at.tugraz.kti.pdftable.document.export;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.print.Doc;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.extract.BlockInfo;
import at.tugraz.kti.pdftable.extract.CharInfo;

/**
 * creates the structur export in ICDAR competition format
 *
 */
public class LogicalStructureExport extends DOMExport {
	
	Element transformTable(DocumentTable t, RichDocument src) throws DocumentException {
		int i, row, col;
		Element table = dom.createElement("table");
		
		// Map cells to a grid
		HashMap<Integer, HashMap<Integer, TableCell>> grid = t.getAsGrid();
		
		// Store logic table.
		ArrayList<ArrayList<HashSet<TableCell>>> logictable 
			= new ArrayList<ArrayList<HashSet<TableCell>>>();
		
		// Loop over cells in grid.
		for ( row = 0; row < t.trs.size(); row++) {
			for ( col = 0; col < t.trs.get(row).size(); col++) {
				
				// Current cell.
				TableCell td = t.trs.get(row).get(col);
				
				// Sets to store the current cell's access cells in.
				HashSet<TableCell> colset = new HashSet<TableCell>();
				HashSet<TableCell> rowset = new HashSet<TableCell>();
				
				// Is cell a header cell itself ? 
				// - if so, ignore.
				if (td.isHeader()) continue;
				
				// Examine other cells in the same row.
				// If there are header-cells, add them as row-headers.
				for(i=0; i < t.getNCols(); i++) {
					if(grid.get(td.startrow).get(i).isHeader()) {
						rowset.add(grid.get(td.startrow).get(i));
					}
				}
				
				// Examine other cells in the same column.
				// If there are header-cells, add them as row-headers.
				for(i=0; i < t.getNRows(); i++) {
					if(grid.get(i).get(td.startcol).isHeader()) {
						colset.add(grid.get(i).get(td.startcol));
					}
				}

				// Add the cell and it's accesspath to the collection of cells
				// ((rowheaders), (colheaders), (datacells))
				ArrayList<HashSet<TableCell>> accesspath = new ArrayList<HashSet<TableCell>>();
				accesspath.add(rowset);
				accesspath.add(colset);
				HashSet<TableCell> dataset = new HashSet<TableCell>();
				dataset.add(td);
				accesspath.add(dataset);
				
				logictable.add(accesspath);
			}
		}
		
		// build xml representation
		for(ArrayList<HashSet<TableCell>> access : logictable) {
			Element e_value = dom.createElement("value");
			table.appendChild(e_value);
			
			// prepare 3 sets of values (2 dim access, 1 datacell)
			ArrayList<ArrayList<String>> dimdata = new ArrayList<ArrayList<String>>();
			for( i=0; i<3; i++) {
				dimdata.add(new ArrayList<String>());
				for (TableCell tdout : access.get(i)) {
					BlockInfo bi = new BlockInfo(src.getCharactersInBoundingBox(t.page, tdout), "");
					dimdata.get(i).add(bi.getCharactersAsString());
				}
			}
			
			Element dimx = dom.createElement("dimension");
			dimx.appendChild(dom.createTextNode(str_join(dimdata.get(0),".")));
			e_value.appendChild(dimx);
			Element dimy = dom.createElement("dimension");
			dimy.appendChild(dom.createTextNode(str_join(dimdata.get(1),".")));
			e_value.appendChild(dimy);
			Element datval = dom.createElement("data");
			datval.appendChild(dom.createTextNode(str_join(dimdata.get(2),",")));
			e_value.appendChild(datval);
			
			// System.out.println(rowaccess + "/" + colaccess + "/" + contents);
		}
		
		return table;
	}
	
	protected void buildDom(RichDocument doc, DocumentTables tdef) throws ParserConfigurationException, DocumentException {
		
		Element document = dom.createElement("document");
		document.setAttribute("filename", doc.getFilename());
		dom.appendChild(document);
		
		int table_id = 0;
		for ( DocumentTable t : tdef.getTables()) {
			Element table = transformTable(t,  doc);
			document.appendChild(table);
			table.setAttribute("tid", Integer.toString(table_id));
			table_id++;
		}
	}
	
	protected String str_join(Collection s, String delimiter) {
		// from http://stackoverflow.com/questions/1515437/java-function-for-arrays-like-phps-join
	    StringBuffer buffer = new StringBuffer();
	    Iterator iter = s.iterator();
	    while (iter.hasNext()) {
	        buffer.append(iter.next());
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}
}
