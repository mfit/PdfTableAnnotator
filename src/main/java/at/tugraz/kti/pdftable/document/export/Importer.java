package at.tugraz.kti.pdftable.document.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jackson.map.introspect.BasicClassIntrospector.GetterMethodFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.DocumentTables;

/**
 * Open and convert a file with table structure annotations in xml format.
 * 
 * @author "matthias frey"
 *
 */
public class Importer {
	float page_height = 842.0f;
	
	RichDocument doc;
	File file;
	
	public void open(File file, RichDocument doc) {
		this.file = file;
		this.doc = doc;
	}
	
	public void open(File file) {
		this.file = file;
	}
	
	protected float getPageHeight(int page) {
		if (doc == null || doc.pages_dimensions.size() < page+1) {
			return page_height;
		} else {
			return doc.pages_dimensions.get(page).getHeight();
		}
	}
	
	public DocumentTables getTableDefinitions(File file, RichDocument doc) throws ParserConfigurationException, SAXException, IOException {
		page_height = doc.pages_dimensions.get(0).getHeight();
		return getTableDefinitions(file);
	}
	
	/**
	 * Open and parse file. Return table definitions.
	 * 
	 * @param file
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public DocumentTables getTableDefinitions(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentTables document_tables = new DocumentTables();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder 	= dbFactory.newDocumentBuilder();
		Document doc 				= dBuilder.parse(file);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("region");
		
		// loop over table-regions ...
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node region = nList.item(temp);
			int maxx = 0, maxy = 0;
			
			if (region.getNodeType() == Node.ELEMENT_NODE) {
				
				//
				// turn region element into a table
				//
				
				DocumentTable tab = new DocumentTable();
				HashMap<Integer, HashMap<Integer, TableCell>> gridmap = new HashMap<Integer, HashMap<Integer, TableCell>>();
				Element region_element = (Element) region;
				NodeList cells = region.getChildNodes();
				
				int page = Integer.parseInt(region_element.getAttribute("page"));
				tab.page = page - 1;
				
				//
				// Loop over cells, adding cells to a "gridmap" (x,y indexed)
				//
				for (int i = 0; i < cells.getLength(); i++) {
					Node cell = cells.item(i);
					if (cell.getNodeType() == Node.ELEMENT_NODE) {
						
						//
						// process a single cell
						//
						
						Element cellElement = (Element) cell;
						Element bbox = (Element) cell.getChildNodes().item(1);
						TableCell td = new TableCell(
							Float.parseFloat(bbox.getAttribute("x1")),
							getPageHeight(tab.page) - Float.parseFloat(bbox.getAttribute("y2")),
							Float.parseFloat(bbox.getAttribute("x2")),
							getPageHeight(tab.page) - Float.parseFloat(bbox.getAttribute("y1"))
						);
						
						int gridy = Integer.parseInt(cellElement.getAttribute("start-row"));
						int gridx = Integer.parseInt(cellElement.getAttribute("start-col"));
						td.startrow = gridy;
						td.startcol = gridx;
						if ( ! gridmap.containsKey(gridy)) {
							gridmap.put(gridy, new HashMap<Integer, TableCell>());
						}
						// store table in a 2dim map indexed by gridx, gridy
						gridmap.get(gridy).put(gridx, td);
						
						// count up to get the underlying gridsize
						if (gridy > maxy) maxy = gridy;
						if (gridx > maxx) maxx = gridx;
					}
				}
				
				//
				// Set cell properties & convert from gridmap to TD/TR based
				// model.
				//
				// TODO : multiple (ROW -) spans are not covered  ! 
				//
				for(int y = 0; y < maxy+1; y++) {
					if (gridmap.containsKey(y)) {
						ArrayList<TableCell> row = new ArrayList<TableCell>(); 
						for(int x = 0; x < maxx+1; x++) {
							if ( gridmap.get(y).containsKey(x)) {
								// Td found at position, add it to row.
								row.add(gridmap.get(y).get(x));
							} else {
								// Empty grid position found.
								if (row.size() > 0 ) {
									// "Stretch" td to the left, if any.
									row.get(row.size()-1).colspan++;
								} else {
									// No td found, add empty td.
									row.add(new TableCell());
								}
							}
						}
						tab.trs.add(row);
					}
				}
				
				// call normalization/maximization
				tab.calculateGridLocation();	// re-calculation / 
												// normalizatin of grid-positions
				tab.maximizeCells();
				
				// add table (region) to page
				if ( ! document_tables.annotationsOnPage.containsKey(tab.page)) {
					document_tables.annotationsOnPage.put(tab.page, new ArrayList<DocumentTable>());
				}
				document_tables.annotationsOnPage.get(tab.page).add(tab);
			}
		}

		return document_tables;
	}
}
