package at.tugraz.kti.pdftable.document.export;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

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
 * Creates export of the table structure, 
 * 		competition-entry-structure-model.xsd
 *
 */
public class StructureExport extends DOMExport {
	
	public String getExportFilename(String docname) {
		return docname + "-str." + getFormat();
	}
	
	protected void buildDom(RichDocument doc, DocumentTables tdef) throws ParserConfigurationException, DocumentException {
		
		Element document = dom.createElement("document");
		document.setAttribute("filename", doc.getFilename());
		dom.appendChild(document);
		
		int table_id = 0;
		for ( DocumentTable t : tdef.getTables()) {
			Element tab = dom.createElement("table");
			tab.setAttribute("id", Integer.toString(table_id));
			Element region = dom.createElement("region");
			region.setAttribute("col-increment", Integer.toString(0));
			region.setAttribute("row-increment", Integer.toString(0));
			region.setAttribute("id", Integer.toString(0)); //no regions, so region id is 0 
			region.setAttribute("page", Integer.toString(t.page + 1)); // add 1 to account for 1-based counting in export
			tab.appendChild(region);
			document.appendChild(tab);
			
			float page_height = doc.pages_dimensions.get(t.page).getHeight();
			int cell_id = 0;
			for ( int i = 0; i < t.trs.size(); i++) {
				for ( int j = 0; j < t.trs.get(i).size(); j++) {
					
					TableCell td = t.trs.get(i).get(j);
					ArrayList<CharInfo> in_chars = doc.getCharactersInBoundingBox(
							t.page, td.visgrid.get(0), td.visgrid.get(1), 
							td.visgrid.get(2), td.visgrid.get(3));
					BlockInfo bi = new BlockInfo(in_chars, "");
					
					Element cell = dom.createElement("cell");
					cell.setAttribute("id", Integer.toString(cell_id));
					cell.setAttribute("start-col", Integer.toString(td.startcol));
					cell.setAttribute("start-row", Integer.toString(td.startrow));
					
					Element bbox = dom.createElement("bounding-box");
					bbox.setAttribute("x1", Integer.toString(Math.round(bi.x)));
					bbox.setAttribute("x2", Integer.toString(Math.round(bi.x + bi.w)));
					
					bbox.setAttribute("y1", Integer.toString(Math.round(page_height - (bi.y + bi.h))));
					bbox.setAttribute("y2", Integer.toString(Math.round(page_height - bi.y)));
					
					Element content = dom.createElement("content");
					content.appendChild(dom.createTextNode(bi.getCharactersAsString()));
					
					cell.appendChild(bbox);
					cell.appendChild(content);
					region.appendChild(cell);
					
					cell_id++;
				}
			}
			table_id++;
		}
	}
}
