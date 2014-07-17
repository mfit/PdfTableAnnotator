package at.tugraz.kti.pdftable.document.export;


import java.util.ArrayList;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.extract.BlockInfo;
import at.tugraz.kti.pdftable.extract.CharInfo;

/**
 * An export in HTML format.
 * Exports annotated tables with content and bounding boxes from PDF document
 * as html tables.
 */
public class HtmlExport extends DOMExport{
	
	public HtmlExport() {
		super();
		format = "html";
	}
	
	public void buildDom(RichDocument doc, DocumentTables tdef) throws ParserConfigurationException, DocumentException {
		
		Element root = dom.createElement("html");
		dom.appendChild(root);
		
		Element body = dom.createElement("body");
		root.appendChild(body);
		
		Element docname_h1 = dom.createElement("h1");
		docname_h1.appendChild(dom.createTextNode(doc.getFilename()));
		body.appendChild(docname_h1);
		
		int table_id = 0;
		for ( DocumentTable t : tdef.getTables()) {
			Element tab 				= dom.createElement("table");
			tab.setAttribute("id", 		Integer.toString(table_id));
			tab.setAttribute("data-page", 	Integer.toString(t.page + 1));		// add one
			tab.setAttribute("border", 	"1");
			
			BlockInfo regionblock = new BlockInfo();
			
			for ( int i = 0; i < t.trs.size(); i++) {
				
				Element tr = dom.createElement("tr");
				tab.appendChild(tr);
				
				for ( int j = 0; j < t.trs.get(i).size(); j++) {
					
					TableCell td = t.trs.get(i).get(j);
					
					ArrayList<CharInfo> in_chars = doc.getCharactersInBoundingBox(
							t.page, td.visgrid.get(0), td.visgrid.get(1), 
							td.visgrid.get(2), td.visgrid.get(3));
					BlockInfo bi = new BlockInfo(in_chars, "");
					
					Element cell = dom.createElement("td");
					cell.setAttribute("rowspan", Integer.toString(td.rowspan));
					cell.setAttribute("colspan", Integer.toString(td.colspan));

					// the minimal bounding box spanned by character content
					cell.setAttribute("data-bbox", "["+
							"minx=" +  Float.toString(bi.x) + "," +
							"maxx=" +  Float.toString(bi.x + bi.w) + "," +
							"miny=" +  Float.toString(bi.y) + "," +
							"maxy=" +  Float.toString(bi.y + bi.h) + "]");
					
					
					// css-style information 
					cell.setAttribute("style","width:"+(td.visgrid.get(2)-td.visgrid.get(0))
							+"px;height:"+(td.visgrid.get(3)-td.visgrid.get(1))+"px");
					
					// the (larger) area that was drawn to mark the cell
					cell.setAttribute("data-cellbox", "[minx="+td.visgrid.get(0)
							+", maxx="+td.visgrid.get(2)
							+", miny="+td.visgrid.get(1)
							+", maxy="+td.visgrid.get(3)+"]");
							
					// Annotated class of cell
					// TODO: support multiple classes per cell. 
					if ( td.classes.size() > 0) {
						cell.setAttribute("data-class", td.classes.get(0));
						cell.setAttribute("class", td.classes.get(0));
					}

					// textual content
					cell.appendChild(dom.createTextNode(bi.getCharactersAsString()));
					
					tr.appendChild(cell);
					regionblock.addBlock(bi);
				}
			}
			
			// bounding box of table region
			tab.setAttribute("data-bbox", "["+
					"minx=" +  Float.toString(regionblock.x) + "," +
					"maxx=" +  Float.toString(regionblock.x + regionblock.w) + "," +
					"miny=" +  Float.toString(regionblock.y) + "," +
					"maxy=" +  Float.toString(regionblock.y + regionblock.h) + "]");
			
			body.appendChild(tab);
			
			table_id++;
		}
	}
	
}
