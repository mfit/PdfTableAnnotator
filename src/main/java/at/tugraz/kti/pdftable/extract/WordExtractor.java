package at.tugraz.kti.pdftable.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import at.tugraz.kti.pdftable.document.RichDocument;

/**
 * Group characters to blocks on the "same line" (y coordinate) and using a 
 * cutoff value for the maxium allowed x-distance. Basic / experimental.
 * 
 * @author "matthias frey"
 */
public class WordExtractor {

	ArrayList<CharInfo> chars = null;
	ArrayList<BlockInfo> blocks = null;
	ArrayList<ArrayList<BlockInfo>> lines = null;

	public void setCharacters(ArrayList<CharInfo> chars) {
		this.chars = chars;
	}

	public ArrayList<BlockInfo> getWords() {
		return blocks;
	}
	
	public ArrayList<ArrayList<BlockInfo>> getLines() {
		return lines;
	}

	public void extractWords() {
		blocks 	= new ArrayList<BlockInfo>();
		lines 	= new ArrayList<ArrayList<BlockInfo>>();

		// put characters with same Y-value together
		HashMap<Float, ArrayList<CharInfo>> buckets = new HashMap<Float, ArrayList<CharInfo>>();
		for (CharInfo ci : chars) {
			if (!buckets.containsKey(ci.y)) {
				buckets.put(ci.y, new ArrayList<CharInfo>());
			}
			buckets.get(ci.y).add(ci);
		}

		// get list of sorted keys
		ArrayList sorted_tops = new ArrayList(new TreeSet(buckets.keySet()));
		Float oldy = new Float(0.0f);
		
		// loop over "buckets" of same-y characters (line-blocks)
		for (Object y : sorted_tops) {
			
			ArrayList<BlockInfo> line = new ArrayList<BlockInfo>();
			
			BlockInfo blockline = new BlockInfo();
			
			// put characters with same X-value together and sort X values
			ArrayList<CharInfo> innerchars = buckets.get((Float) y);
			HashMap<Float, ArrayList<CharInfo>> xsortbuckets = new HashMap<Float, ArrayList<CharInfo>>();
			for (CharInfo ci : innerchars) {
				if (!xsortbuckets.containsKey(ci.x)) {
					xsortbuckets.put(ci.x, new ArrayList<CharInfo>());
				}
				xsortbuckets.get(ci.x).add(ci);
			}
			ArrayList sortedx = new ArrayList(
					new TreeSet(xsortbuckets.keySet()));

			// characters sorted by X
			ArrayList<CharInfo> sorted_chars = new ArrayList<CharInfo>();
			for (Object x : sortedx) {
				for (CharInfo ci : xsortbuckets.get(x)) {
					sorted_chars.add(ci);
				}
			}

			// cluster characters in words depending on space between
			Float oldx = null;
			float current_space = 0;
			ArrayList<ArrayList<CharInfo>> words = new ArrayList<ArrayList<CharInfo>>();
			ArrayList<CharInfo> current_word = new ArrayList<CharInfo>();
			// float space_limit = getWordBoundarySpaceLimit();
			float space_limit = getWordBoundarySpaceLimit(sorted_chars);

			for (CharInfo ci : sorted_chars) {
				if (oldx != null) {
					current_space = ci.x - oldx;
				}
				// determine whether new word has started
				if (current_space > space_limit) {
					words.add(current_word);
					current_word = new ArrayList<CharInfo>();
				}
				current_word.add(ci); // add character to current word
				oldx = ci.x + ci.w; // set new char-end-position (x + widht)
			}
			words.add(current_word); // add last word

			// add every word as a block
			for (ArrayList<CharInfo> charsinword : words) {
				String text = "";
				for (CharInfo ci : charsinword) {
					text = text + ci.c;
				}

				BlockInfo word = new BlockInfo(charsinword, text);
				blocks.add(word);
				line.add(word);
				blockline.addBlock(word);
			}

			lines.add(line);
		} // line (same-y)
	}

	/**
	 * return a suitable word-boundary character spacing limit (default value)
	 * 
	 * @return
	 */
	protected float getWordBoundarySpaceLimit() {
		return 1.0f;
	}

	/**
	 * return a suitable word-boundary character spacing limit by examinig a
	 * sorted list of characters
	 * 
	 * @param sorted_chars
	 * @return
	 */
	protected float getWordBoundarySpaceLimit(ArrayList<CharInfo> sorted_chars) {
		float width_sum = 0;
		for (CharInfo ci : sorted_chars) {
			width_sum += ci.w;
		}
		
		return (width_sum / sorted_chars.size()) / 4;
		
	}

	public static void main(String[] args) throws Exception {
		RichDocument doc = new RichDocument("resources/src/2500199.pdf");
		doc.open();
		WordExtractor we = new WordExtractor();
		we.setCharacters(doc.getCharacters(0));
		we.extractWords();
		for (BlockInfo bi : we.getWords()) {
			System.out.println(bi.text);
		}

	}

}
