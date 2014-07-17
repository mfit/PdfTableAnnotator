package at.tugraz.kti.pdftable.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
 * Read characters from PDF.
 */
public class ParsePdf extends PDFTextStripper {

	PDDocument document = null;

	protected ArrayList<CharInfo> collect_chars_info;
	
	/**
	 * store size / dimensions for every page
	 */
	protected ArrayList<PDRectangle> pagesizes;
	
	/**
	 * store character info per page
	 */
	protected ArrayList<ArrayList<CharInfo>> allchars = new ArrayList<ArrayList<CharInfo>>();

	/**
	 * Default constructor.
	 * @throws IOException
	 *             If there is an error loading text stripper properties.
	 */
	public ParsePdf(String filename) throws IOException {
		super.setSortByPosition(true);
		document = PDDocument.load(filename);
	}

	/**
	 * read character information , pagesizes
	 * of all annotationsOnPage of the document
	 * 
	 * @throws Exception
	 */
	public void parse() throws Exception {
		
		allchars = new ArrayList<ArrayList<CharInfo>>();
		pagesizes = new ArrayList<PDRectangle>();

		try {
			List allPages = document.getDocumentCatalog().getAllPages();
			for (int i = 0; i < allPages.size(); i++) {
				allchars.add(new ArrayList<CharInfo>());
				collect_chars_info = allchars.get(i);
				PDPage page = (PDPage) allPages.get(i);
				pagesizes.add(page.findMediaBox());
				PDStream contents = page.getContents();
				if (contents != null) {
					processStream(page, page.findResources(), page
							.getContents().getStream());
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (document != null) {
				document.close();
			}
		}		
	}

	public ArrayList<ArrayList<CharInfo>> getCharacterInfo() {
		return allchars;
	}
	
	public ArrayList<PDRectangle> getPageDimensions() {
		return pagesizes;
	}

	/**
	 * A method provided as an event interface to allow a subclass to perform
	 * some specific functionality when text needs to be processed.
	 * 
	 * @param text
	 *            The text to be processed
	 */
	protected void processTextPosition(TextPosition text) {
		CharInfo c = new CharInfo(text.getCharacter(), text.getXDirAdj(),
				text.getYDirAdj());
		c.xscale = text.getXScale();
		c.yscale = text.getYScale();
		c.w = text.getWidthDirAdj();
		c.h = text.getHeightDir();

		// text.getFontSize()
		// text.getWidthOfSpace()

		collect_chars_info.add(c);
	}

}
