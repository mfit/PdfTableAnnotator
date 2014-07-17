package at.tugraz.kti.pdftable.extract;

import java.util.ArrayList;

/**
 * Representing a "block", a container for single characters @see CharInfo
 * @author Matthias Frey
 *
 */
public class BlockInfo {
	public float x = 0;
	public float y = 0;
	public ArrayList<CharInfo> chrs;
	public float w = 0;
	public float h = 0;
	
	public ArrayList<BlockInfo> children;
	
	public String text;
	
	public BlockInfo() {
		chrs = new ArrayList<CharInfo>();
		children = new ArrayList<BlockInfo>();
	}

	public BlockInfo(ArrayList<CharInfo> chars, String text) {
		block_init(chars, text);
	}
	
	public BlockInfo(ArrayList<CharInfo> chars) {
		block_init(chars, "");
	}
	
	protected void block_init(ArrayList<CharInfo> chars, String text) {
		this.chrs = chars;
		this.text = text;
		
		if (chars != null && chars.size()>0) {
			// init top / left to first char
			x = chars.get(0).x;
			y = chars.get(0).y;
			
			// Characters have baseline coordinates and "count upwards" for the height
			for (CharInfo ci : chars) {
				float cury = ci.y - ci.h;
				if (ci.x < x ) x = ci.x;
				if (cury < y ) y = cury;
				if ((ci.x+ci.w)-x > w) w=(ci.x+ci.w)-x;
				if (ci.y-y > h) h=ci.y-y;
			}
		}
	}
	
	public void addBlock(BlockInfo b)
	{
		if (children.size() == 0 && w == 0) { // empty block , initalize to first block
			x = b.x;
			y = b.y;
			w = b.w;
			h = b.h;
		}
		
		children.add(b);
		
		// calc new bounding box
		if ( b.x < x) {
			x = b.x;
			w += x - b.x;
		}
		if ( b.y < y) {
			y = b.y;
			h += y - b.h;
		}
		
		if ( b.x + b.w > x + w) w = b.x + b.w - x;
		if ( b.y + b.h > y + h) h = b.y + b.h - y;
		
	}
	
	public void calculateText()
	{
		text = "";
		for (BlockInfo b : children) {
			text = text + b.text + " ";
		}
	}
	
	
	public String getCharactersAsString()
	{
		String s = "";
		for (CharInfo ci : chrs)
		{
			s += ci.c;
		}
		return s;
	}
}
