package at.tugraz.kti.pdftable.document.export;


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
 * Creates export of table regions.
 * 		competition-entry-region-model.xsd
 *
 */
public class RegionExport extends DOMExport {
	
	public String getExportFilename(String docname) {
		return docname + "-reg." + getFormat();
	}
	
	public void buildDom(RichDocument doc, DocumentTables tdef) throws ParserConfigurationException, DocumentException {
		
		Element document = dom.createElement("document");
		document.setAttribute("filename", doc.getFilename());
		dom.appendChild(document);
		
		//
		// TODO : add the following attr's to root element (document):
		//
		//				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		//				xsi:noNamespaceSchemaLocation="competition-entry-region-model.xsd"
		
		
		int table_id = 0;
		for ( DocumentTable t : tdef.getTables()) {
			Element tab = dom.createElement("table");
			tab.setAttribute("id", Integer.toString(table_id));
			Element region = dom.createElement("region");
			region.setAttribute("id", Integer.toString(0));
			region.setAttribute("page", Integer.toString(t.page));
			tab.appendChild(region);
			document.appendChild(tab);
			
			ArrayList<CharInfo> region_chars = new ArrayList<CharInfo>();
			for ( int i = 0; i < t.trs.size(); i++) {
				for ( int j = 0; j < t.trs.get(i).size(); j++) {
					// initialise td and get its contents (characters) 
					TableCell td = t.trs.get(i).get(j);
					region_chars.addAll(doc.getCharactersInBoundingBox(
							t.page, td.visgrid.get(0), td.visgrid.get(1), 
							td.visgrid.get(2), td.visgrid.get(3)));
				}
			}
			
			BlockInfo region_block = new BlockInfo(region_chars, "");
			Element bbox = dom.createElement("bounding-box");
			bbox.setAttribute("x1", Float.toString(region_block.x));
			bbox.setAttribute("x2", Float.toString(region_block.x + region_block.w));
			bbox.setAttribute("y1", Float.toString(region_block.y));
			bbox.setAttribute("y2", Float.toString(region_block.y + region_block.h));
			region.appendChild(bbox);
			
			table_id++;
		}
	}
}
