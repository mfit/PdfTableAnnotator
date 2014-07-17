package at.tugraz.kti.pdftable.extract;

import java.util.ArrayList;

import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;

/**
 * Auto-detection feature. Experimental.
 * @author "matthias frey"
 *
 */
public class TableDetectionFromRegion {

	public DocumentTable extract(RichDocument doc, int page, ArrayList<Float> region) throws DocumentException {
		
		DocumentTable tab = new DocumentTable();
		tab.page = page;
		
		ArrayList<CharInfo> chars = doc.getCharactersInBoundingBox(page,
				region.get(0), region.get(1), region.get(2), region.get(3));
		
		// cluster characters to words and lines
		WordExtractor we = new WordExtractor();
		we.setCharacters(chars);
		we.extractWords();
		ArrayList<ArrayList<BlockInfo>> lines = we.getLines();
		
		// from words in lines, determine a table grid 
		for(ArrayList<BlockInfo> tds : lines) {
			ArrayList<TableCell> tr = new ArrayList<TableCell>();
			BlockInfo bi = tds.get(0);
			tr.add(new TableCell(bi.x, bi.y, bi.x + bi.w, bi.y + bi.h));
			tab.trs.add(tr);
		}
		
		// 
		// stretch tds vertically ( = convert from inner boundingboxes to 
		// 	to grid ) 
		// 
		float lasty = 0;
		for ( int j = 0; j < tab.trs.size()-1; j++) {
			TableCell td_cur 	= tab.trs.get(j).get(0);
			TableCell td_next 	= tab.trs.get(j+1).get(0);
			td_cur.visgrid.set(3, td_next.visgrid.get(1));
		}
		
/*
 * we should : 
 * 
 * get our y values like this : 
 * beginblock, endblock, beginblock, endblock
 * take the middle between each endblock-beginblock
 * add some slack at bottom + top ,but maximum to the boundaries of the original
 * region - or, rather maybe exactly from / up to the original region ) 
 * 
 * how to find "sub-rows" that need to be merged ? 
 * 
 * how to do splitting on the horizontal axis ? 
 * 		=> list all x values that form spaces between words over all TRs
 * 		=> take those, that ... have  certain sizes,
 * 							... occur on many / all rows ! 
 * 
 * ( algorithm that works from parts upwards, and merges parts to one cell if
 * that seems reasonable ..)
 * 		
 */
		
		return tab;
	}
}
