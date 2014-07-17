package at.tugraz.kti.pdftable.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Internal table model.
 * 
 * @author "matthias frey"
 *
 */
public class DocumentTable {
	
	/**
	 * Global "max-id".
	 */
	@JsonIgnore
	static public int _tn = 0;
	
	/**
	 * The page the table is on.
	 */
	public int page = 0;
	
	/**
	 * The table's (internal) id.
	 */
	public int tn = 0;
	
	/**
	 * The table's cells, in the form of an array of rows, 
	 * which each is an array of cells.
	 */
	public ArrayList<ArrayList<TableCell>> trs;
	
	public DocumentTable() {
		tn = getNextId();
		trs = new ArrayList<ArrayList<TableCell>>();
	}
	
	public static DocumentTable createTable() {
		DocumentTable tab = new DocumentTable();
		tab.tn = getNextId();
		return tab;
	}
	
	/**
	 * @return A new global id for a table.
	 */
	@JsonIgnore
	public static synchronized int getNextId() {
		_tn++;
		return _tn;	
	}
	

	/**
	 * Create a (canonicalized) grid-representation of the table, where every 
	 * cell in an grid[int_row][int_col] holds a reference to the TableCell cell 
	 * that is at that position. 
	 * If colspan or rowspan is greater one, two or more positions in the grid
	 * can point to the same cell. 
	 */
	@JsonIgnore
	public HashMap<Integer, HashMap<Integer, TableCell>> getAsGrid() {
		HashMap<Integer, HashMap<Integer, TableCell>> grid;
		grid = new HashMap<Integer, HashMap<Integer, TableCell>>(); 
		for ( int i = 0; i < trs.size(); i++) {
			for ( int j = 0; j < trs.get(i).size(); j++) {
				TableCell td = trs.get(i).get(j);
				for (int r = td.startrow; r < td.startrow+td.rowspan; r++) {
					if (!grid.containsKey(r)) {
						grid.put(r, new HashMap<Integer, TableCell>());
					}
					for (int c = td.startcol; c < td.startcol+td.colspan; c++) {
						grid.get(r).put(c, td);
					}
				}
			}
		}
		return grid;
	}
	
	/**
	 * Given that col- and rowspan are set correctly for every cell,
	 * this method calculates the location of each cell in the n,m grid.
	 */
	public void calculateGridLocation() {
		int sizey = trs.size();
		int sizex = 0;
		int i,j = 0;
		
		for ( i=0; i < trs.get(0).size(); i++) {
			sizex+=trs.get(0).get(i).colspan;
		}
		
		int gridhelper[][] = new int[sizey][sizex];
		
		for ( j = 0; j < sizey; j++) {
			Iterator<TableCell> it  = trs.get(j).iterator();
			i=0;
			while (i < sizex && it.hasNext()) {
				// System.out.println("Current:" + (j) + "," + (i) + "=" + gridhelper[j][i]);
				while (gridhelper[j][i] != 0) {
					i++;
				}
				TableCell td = it.next();
				for (int k=1; k<td.rowspan;k++) {
					for(int l=0; l<td.colspan;l++) {
						gridhelper[j+k][i+l] = 1;
					}
				}
				td.startrow = j;
				td.startcol = i;
				i+=td.colspan;
			}
		}
	}
	
	@JsonIgnore
	public int getNRows() {
		return trs.size();
	}
	
	@JsonIgnore
	public int getNCols() {
		if(trs.size()>0)
			return trs.get(0).size();
		else return 0; 
	}
	
	/**
	 * Convert from minimal bounding boxes to a "border-collapse" grid.
	 * 
	 * Work with the left / top boundaries of cell, so that cells "expand". 
	 * 
	 * 
	 */
	public void maximizeCells() {
		HashMap<Integer, HashMap<Integer, TableCell>> grid = getAsGrid();
		int sizey = grid.size();
		int sizex = grid.get(0).size();
		TableCell td;
		HashMap<Integer,ArrayList<Float>> left 		= new HashMap<Integer,ArrayList<Float>>();
		HashMap<Integer,ArrayList<Float>> right 	= new HashMap<Integer,ArrayList<Float>>();
		HashMap<Integer,ArrayList<Float>> top 		= new HashMap<Integer,ArrayList<Float>>();
		HashMap<Integer,ArrayList<Float>> bottom	= new HashMap<Integer,ArrayList<Float>>();
		HashMap<Integer,Float> slices_x	= new HashMap<Integer,Float>();
		HashMap<Integer,Float> slices_y	= new HashMap<Integer,Float>();
		
		for (int j=0; j<sizey; j++) {
			top.put(j, new ArrayList<Float>());
			bottom.put(j, new ArrayList<Float>());
		}
		for ( int i=0; i<sizex; i++) {
			left.put(i, 	new ArrayList<Float>());
			right.put(i, 	new ArrayList<Float>());
		}
		
		//
		// Collect all cell dimensions
		// ! depends on correctly set start col/row !
		//
		// 
		for ( int j = 0 ; j < sizey; j++) {
			for ( int i = 0 ; i < sizex; i++) {
				try {
					td = grid.get(j).get(i);
					if ( td.hasSize() ) {
						if(td.startcol == i) {
							left.get(i).add(td.visgrid.get(0));
						}
						if((td.startcol + td.colspan - 1) == i) {
							right.get(i).add(td.visgrid.get(2));
						}
						
						if(td.startrow == j) {
							top.get(j).add(td.visgrid.get(1));
						}
						
						if((td.startrow + td.rowspan - 1 )== j) {
							bottom.get(j).add(td.visgrid.get(3));
						}
					}					
				} catch (NullPointerException e) {
					System.out.println("Cell missing at " + j + "," + i );
				}
			}
		}
		
		float grid_slice;
		// Determine x - grid lines (slices)
		slices_x.put(0, Collections.min(left.get(0)) - 1.0f);
		for ( int i = 1 ; i < sizex; i++) {
			grid_slice = (Collections.max(right.get(i-1)) + Collections.min(left.get(i)))/2;
			slices_x.put(i, grid_slice);
		}
		slices_x.put(sizex, Collections.max(right.get(sizex-1)) + 1.0f);
		
		// Determine y-grid lines
		slices_y.put(0, Collections.min(top.get(0)) - 1.0f);
		for ( int j = 1 ; j < sizey; j++) {
			grid_slice = Collections.max(bottom.get(j-1));
			if(top.containsKey(j) && top.get(j).size()>0) {
				grid_slice += Collections.min(top.get(j));
				grid_slice /= 2;
			}
			slices_y.put(j, grid_slice);
		}
		if ( bottom.get(sizey-1).size() > 0 ) {
			slices_y.put(sizey, Collections.max(bottom.get(sizey-1))+1.0f);
		}
		
		// Set slice dimensions to cells.
		for ( int j = 0 ; j < sizey; j++) {
			for ( int i = 0 ; i < sizex; i++) {
				
				td = grid.get(j).get(i);
				if(td.hasSize()) {
					if(td.startcol == i) {
						td.visgrid.set(0, slices_x.get(i));
					}
					if((td.startcol + td.colspan - 1) == i) {
						td.visgrid.set(2, slices_x.get(i+1));
					}
					
					if(td.startrow == j) {
						td.visgrid.set(1, slices_y.get(j));
					}
					
					if((td.startrow + td.rowspan - 1 )== j) {
						td.visgrid.set(3, slices_y.get(j+1));
					}
				}
			}
		}
		
	}
}
