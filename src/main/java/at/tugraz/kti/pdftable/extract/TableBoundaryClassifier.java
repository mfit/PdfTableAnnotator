package at.tugraz.kti.pdftable.extract;

import java.util.ArrayList;

/**
 * Detect region from sparse lines. Experimental. 
 * @author "matthias frey"
 */
public class TableBoundaryClassifier {

	ArrayList<ArrayList<BlockInfo>> lines;
	ArrayList<ArrayList<BlockInfo>> table_lines;

	public void setLines(ArrayList<ArrayList<BlockInfo>> lines) {
		this.lines = lines;
	}

	public void classifyLines() {
		table_lines = new ArrayList<ArrayList<BlockInfo>>();
		
		for ( ArrayList<BlockInfo> line : lines) {
			
			float blockstart = 9999999;
			float blockend = 0;
			float sum_of_lengths_of_words = 0;

			for (BlockInfo word : line)
			{
				if ( word.x < blockstart) blockstart = word.x;
				if ( word.x + word.w > blockend) blockend = word.x + word.w;
				sum_of_lengths_of_words+= word.w;
			}
			
			float sparselinefactor = sum_of_lengths_of_words /  (blockend - blockstart); 
			if  (sparselinefactor < 0.5) { table_lines.add(line); }
		}
	}

	public ArrayList<ArrayList<BlockInfo>> getTableLines() {
		return table_lines;
	}

}
