package at.tugraz.kti.pdftable.document;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Represents a table cell.
 * @author "matthias frey"
 */
public class TableCell {

	public static String HEADER_HEADER 	= "header";
	public static String HEADER_ROW 	= "rowhead";
	public static String HEADER_COL 	= "colhead";
	public static String HEADER_CAT 	= "stubhead";
	
	/**
	 * Dimensions of cell, as [x1, y1, x2, y2]. 
	 */
	public ArrayList<Float> visgrid;
	
	/**
	 * Allows to tag cell as beeing a header cell.
	 */
	public ArrayList<String> classes;
	
	public int rowspan = 1;
	public int colspan = 1;
	
	/**
	 * Row grid position. Set via javascript frontend.
	 */
	public int startrow;
	
	/**
	 * Col grid position. Set via javascript frontend.
	 */
	public int startcol;
	

	public TableCell() {
		visgrid = new ArrayList<Float>(); // x1, y1, x2, y2
		classes = new ArrayList<String>();
		visgrid.add(-1.0f);
		visgrid.add(-1.0f);
		visgrid.add(-1.0f);
		visgrid.add(-1.0f);
	}

	public TableCell(float x1, float y1, float x2, float y2) {
		visgrid = new ArrayList<Float>();
		visgrid.add(x1);
		visgrid.add(y1);
		visgrid.add(x2);
		visgrid.add(y2);
		classes = new ArrayList<String>();
	}

	public TableCell(ArrayList<Float> visg) {
		this(visg.get(0), visg.get(1), visg.get(2), visg.get(3));
	}

	@JsonIgnore
	public boolean isHeader() {
		if (classes.size() > 0) {
			for (String cls : classes) {
				if (cls == null)
					continue;
				if (cls.equals(HEADER_COL) || cls.equals(HEADER_ROW)
						|| cls.equals(HEADER_CAT) || cls.equals(HEADER_HEADER)) {
					return true;
				}
			}
		}
		return false;
	}

	@JsonIgnore
	public float getDim(String what) {
		if (what.equals("x1")) return visgrid.get(0);
		else if (what.equals("x2")) return visgrid.get(2);
		else if (what.equals("y1")) return visgrid.get(1);
		else if (what.equals("y2")) return visgrid.get(3);
		else throw new RuntimeException("Invalid dimension");
	}
	
	@JsonIgnore
	public void setDim(String what, float val) {
		if (what.equals("x1")) visgrid.set(0,val);
		else if (what.equals("x2")) visgrid.set(2,val);
		else if (what.equals("y1")) visgrid.set(1,val);
		else if (what.equals("y2")) visgrid.set(3,val);
		else throw new RuntimeException("Invalid dimension");
	}
	
	@JsonIgnore
	public boolean hasSize() {
		if ( visgrid.get(2) - visgrid.get(0) < 0.1 || visgrid.get(3) - visgrid.get(1) < 0.1)
			return false;
		return true;
	}
	
	@JsonIgnore
	public String toString() {
		return "TD (" + visgrid + ")" + (hasSize() ? "" : "*");
	}
	
}
