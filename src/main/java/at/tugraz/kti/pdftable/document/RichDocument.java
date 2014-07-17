package at.tugraz.kti.pdftable.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

import at.tugraz.kti.pdftable.extract.CharInfo;
import at.tugraz.kti.pdftable.extract.ParsePdf;

/**
 * Represents a PDF-document
 * Represents a PDF-document or the parts useful for and created through 
 * parsing, extracting manual annotation, and similar. 
 * 
 * Stores:
 * 	- single character info (per page with position and size)
 *  - table annotations
 * 
 * @author "matthias frey"
 *
 */
public class RichDocument {

	protected String sourcePath;
	protected String title = "";
	public ArrayList<ArrayList<CharInfo>> charactersOnPage;
	public ArrayList<PDRectangle> pages_dimensions;
	protected int active_page;
	
	protected DocumentTables tableAnnotations;
	
	public RichDocument(String path) {
		this.sourcePath = path;
		charactersOnPage = new ArrayList<ArrayList<CharInfo>>();
		active_page = 0;
	}
	
	public String getFilename() {
		File f = new File(sourcePath);
		return f.getName();
	}
	
	public File getSourcePath() {
		return new File(sourcePath);
	}
	
	public int getNumberOfPages() {
		return charactersOnPage.size();
	}
	
	public RichDocument setActivePage(int p) throws Exception {
		if (p >= getNumberOfPages()) throw new Exception("page exceeds document length");
		active_page = p;
		return this;
	}
	
	public void open() throws IOException {
		ParsePdf textstripper 	= new ParsePdf(sourcePath);
		try {
			textstripper.parse();
			charactersOnPage = textstripper.getCharacterInfo();
			pages_dimensions = textstripper.getPageDimensions();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * get all characters on active page  within a specified bounding box 
	 * 
	 * @param minx
	 * @param miny
	 * @param maxx
	 * @param maxy
	 * @return
	 * @throws DocumentException 
	 */
	public ArrayList<CharInfo> getCharactersInBoundingBox(float minx, float miny, float maxx, float maxy) throws DocumentException {
		return getCharactersInBoundingBox(active_page, minx, miny, maxx, maxy);
	}

	/**
	 * Get all characters on certain page within a specified bounding box.
	 * Simply loop over all characters and selects those with top/left 
	 * coordinate inside the area. 
	 * 
	 * @param page
	 * @param minx
	 * @param miny
	 * @param maxx
	 * @param maxy
	 * @return
	 * @throws DocumentException 
	 */
	public ArrayList<CharInfo> getCharactersInBoundingBox(int page, float minx, float miny, float maxx, float maxy) throws DocumentException {
		
		// Result will be a list of CharInfo 
		ArrayList<CharInfo> res = new ArrayList<CharInfo>();

		// Work on characters of specified page, make sure that page exists.
		if ( page >= getNumberOfPages()) {
			throw new DocumentException("page " + page + 
					" does not exist ("+getNumberOfPages()+" annotationsOnPage)");
		}
		ArrayList<CharInfo> src = charactersOnPage.get(page);
		
		// Simply loop over all characters and select those with top/left
		// coordinate inside the area.
		for (CharInfo c : src) {
			if (c.y >= miny && (c.y /* + c.h */) < maxy && c.x >= minx
					&& (c.x /* + c.w */) < maxx) {
				res.add(c);
			}
		}

		return res;
	}
	
	public ArrayList<CharInfo> getCharactersInBoundingBox(int page, TableCell td) throws DocumentException {
		return getCharactersInBoundingBox(page, 
				td.visgrid.get(0),
				td.visgrid.get(1),
				td.visgrid.get(2),
				td.visgrid.get(3));
	}
	
	public ArrayList<CharInfo> getCharacters(int page) {
		return charactersOnPage.get(page);
	}
	
	public DocumentTables getTableAnnotations() {
		return tableAnnotations;
	}
	
	public void setTableAnnotations(DocumentTables tdefs) {
		tableAnnotations = tdefs;
	}
	
}
