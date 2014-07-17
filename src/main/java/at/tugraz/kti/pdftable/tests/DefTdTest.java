package at.tugraz.kti.pdftable.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import at.tugraz.kti.pdftable.document.TableCell;

public class DefTdTest {

	@Test
	public void test() {
		TableCell td = new TableCell();
		assertFalse(td.hasSize());

		td = new TableCell(0,0,0,0);
		assertFalse(td.hasSize());
		
		td = new TableCell(0,0,0,244);
		assertFalse(td.hasSize());
		
		td = new TableCell(1,1,1,1);
		assertFalse(td.hasSize());
	}

}
