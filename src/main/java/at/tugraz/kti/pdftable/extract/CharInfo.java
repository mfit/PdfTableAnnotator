package at.tugraz.kti.pdftable.extract;

/**
 * A data object that holds information about a single character.
 */
public class CharInfo implements Comparable<CharInfo> {
	public String c;
	public float x;
	public float y;
	public float xscale;
	public float yscale;
	public float w;
	public float h;

	public CharInfo(String c, float x, float y) {
		this.x = x;
		this.y = y;
		this.c = c;
	}

	public int compareTo(CharInfo o) {
		if (y < o.y)
			return -1;
		if (y == o.y)
			return 0;
		return 1;
	}

	public String toString() {
		return c + " at " + "(" + x + "," + y + ")" + ",(" + w + "," + h + ")"
				+ xscale;
	}
}
