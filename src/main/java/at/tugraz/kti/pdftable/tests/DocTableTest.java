package at.tugraz.kti.pdftable.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.extract.CharInfo;
import at.tugraz.kti.pdftable.document.export.StructureExport;

public class DocTableTest {

	@Test
	public void testCalculateGridPos() {
		DocumentTable tab;
		TableCell td;
		
		tab = new DocumentTable();
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		
		// row 0 
		td = new TableCell(0,0,1,2);
		td.rowspan=2;
		td.colspan=1;
		tab.trs.get(0).add(td);
		
		td = new TableCell(1,0,3,1);
		td.colspan=2;
		tab.trs.get(0).add(td);

		// row 1
		td = new TableCell(1,1,2,3);
		tab.trs.get(1).add(td);
		td.rowspan=2;
		td = new TableCell(2,1,3,2);
		tab.trs.get(1).add(td);

		// row 2
		td = new TableCell(0,2,1,3);
		tab.trs.get(2).add(td);
		td = new TableCell(2,2,3,3);
		tab.trs.get(2).add(td);
		
		tab.calculateGridLocation();
		
		org.junit.Assert.assertEquals(0, tab.trs.get(0).get(0).startcol);
		org.junit.Assert.assertEquals(0, tab.trs.get(0).get(0).startrow);
		org.junit.Assert.assertEquals(1, tab.trs.get(0).get(1).startcol);
		org.junit.Assert.assertEquals(0, tab.trs.get(0).get(1).startrow);
		
		org.junit.Assert.assertEquals(1, tab.trs.get(1).get(0).startcol);
		org.junit.Assert.assertEquals(1, tab.trs.get(1).get(0).startrow);
		org.junit.Assert.assertEquals(2, tab.trs.get(1).get(1).startcol);
		org.junit.Assert.assertEquals(1, tab.trs.get(1).get(1).startrow);
		
		org.junit.Assert.assertEquals(0, tab.trs.get(2).get(0).startcol);
		org.junit.Assert.assertEquals(2, tab.trs.get(2).get(0).startrow);
		org.junit.Assert.assertEquals(2, tab.trs.get(2).get(1).startcol);
		org.junit.Assert.assertEquals(2, tab.trs.get(2).get(1).startrow);
		
		tab.maximizeCells();
	}
	
	@Test
	public void testGetGridForMultiRowspan() {
		TableCell td;
		DocumentTable tab = new DocumentTable();
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		
		// row 0
		td = new TableCell(1,1,1,1);
		td.startcol = 0;
		td.startrow = 0;
		td.rowspan=2;
		tab.trs.get(0).add(td);
		
		td = new TableCell(4,2,4,2);
		td.startcol = 1;
		td.startrow = 0;
		tab.trs.get(0).add(td);
		
		// row 1
		td = new TableCell(9,9,9,9);
		td.startcol = 1;
		td.startrow = 1;
		tab.trs.get(1).add(td);
		
		HashMap<Integer, HashMap<Integer, TableCell>> grid = tab.getAsGrid();
		org.junit.Assert.assertEquals(1.0,grid.get(0).get(0).visgrid.get(0),0.1);
		org.junit.Assert.assertEquals(1.0,grid.get(0).get(0).visgrid.get(1),0.1);
		org.junit.Assert.assertEquals(4.0,grid.get(0).get(1).visgrid.get(0),0.1);
		org.junit.Assert.assertEquals(2.0,grid.get(0).get(1).visgrid.get(1),0.1);
		
		org.junit.Assert.assertEquals(1.0,grid.get(1).get(0).visgrid.get(0),0.1);
		org.junit.Assert.assertEquals(1.0,grid.get(1).get(0).visgrid.get(1),0.1);
		org.junit.Assert.assertEquals(9.0,grid.get(1).get(1).visgrid.get(0),0.1);
		org.junit.Assert.assertEquals(9.0,grid.get(1).get(1).visgrid.get(1),0.1);
			
		
	}
	
	@Test
	public void testVerticalCellExpansion()  {
		
		DocumentTable tab = new DocumentTable();
		TableCell td;
		
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		
		td = new TableCell(100,100,290,120);
		tab.trs.get(0).add(td);
		
		td = new TableCell(310,110,380,125);
		tab.trs.get(0).add(td);
		
		td = new TableCell(110,132,295,140);
		tab.trs.get(1).add(td);
		
		td = new TableCell(305,130,387,144);
		tab.trs.get(1).add(td);
		
		tab.trs.get(2).add(new TableCell());
		tab.trs.get(2).add(new TableCell(300,150,379,165));
		
		tab.calculateGridLocation();
		tab.maximizeCells();
		
		// 
		org.junit.Assert.assertEquals(100.0f,  (float) tab.trs.get(0).get(0).visgrid.get(1), 0.1);
		org.junit.Assert.assertEquals(100.0f,  (float) tab.trs.get(0).get(1).visgrid.get(1), 0.1);
		
		org.junit.Assert.assertEquals(127.5f,  (float) tab.trs.get(0).get(0).visgrid.get(3), 1.0);
		org.junit.Assert.assertEquals(127.5f,  (float) tab.trs.get(0).get(1).visgrid.get(3), 1.0);
		
		org.junit.Assert.assertEquals(127.5f,  (float) tab.trs.get(1).get(0).visgrid.get(1), 1.0);
		org.junit.Assert.assertEquals(127.5f,  (float) tab.trs.get(1).get(1).visgrid.get(1), 1.0);
		org.junit.Assert.assertEquals(147.0f,  (float) tab.trs.get(1).get(0).visgrid.get(3), 1.0);
		org.junit.Assert.assertEquals(147.0f,  (float) tab.trs.get(1).get(1).visgrid.get(3), 1.0);
		
		org.junit.Assert.assertEquals(147.0f,  (float) tab.trs.get(2).get(0).visgrid.get(1), 1.0);
		org.junit.Assert.assertEquals(165.0f,  (float) tab.trs.get(2).get(0).visgrid.get(3), 1.0);
		
	}
	
	@Test
	public void testHorizontalExpansion()  {
		DocumentTable tab = new DocumentTable();
		
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		
		tab.trs.get(0).add(new TableCell(120,100,145,150));
		tab.trs.get(0).add(new TableCell(550,95,580,118));
		
		tab.trs.get(1).add(new TableCell(110,210,345,225));
		tab.trs.get(1).add(new TableCell(530,210,570,230));
		
		tab.calculateGridLocation();
		tab.maximizeCells();
		
		org.junit.Assert.assertEquals(110.0f,  (float) tab.trs.get(0).get(0).visgrid.get(0), 0.1);
		org.junit.Assert.assertEquals(110.0f,  (float) tab.trs.get(1).get(0).visgrid.get(0), 0.1);
		
//		org.junit.Assert.assertEquals(110.0f,  (float) tab.trs.get(0).get(0).visgrid.get(0), 0.1);
//		org.junit.Assert.assertEquals(110.0f,  (float) tab.trs.get(1).get(0).visgrid.get(0), 0.1);
		
		//org.junit.Assert.assertEquals(127.5f,  (float) tab.trs.get(1).get(1).visgrid.get(1), 0.1);
	}

	@Test
	public void testHorizontalExpansionWithThreeCells()  {
		DocumentTable tab = new DocumentTable();
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.get(0).add(new TableCell(10,10,50,20));
		tab.trs.get(0).add(new TableCell(100,10,120,20));
		tab.trs.get(0).add(new TableCell(400,10,420,20));
		
		tab.maximizeCells();
		
		// TODO : expansion between cell(n-1).end and cell(n).start is not 
		//		defined yet
		
		org.junit.Assert.assertEquals(10.0f,  (float) tab.trs.get(0).get(0).visgrid.get(0), 0.1);
		//org.junit.Assert.assertEquals(75.0f,  (float) tab.trs.get(0).get(0).visgrid.get(2), 0.1);
		//org.junit.Assert.assertEquals(260.0f,  (float) tab.trs.get(0).get(1).visgrid.get(2), 0.1);
		org.junit.Assert.assertEquals(400.0f,  (float) tab.trs.get(0).get(2).visgrid.get(0), 0.1);
		org.junit.Assert.assertEquals(420.0f,  (float) tab.trs.get(0).get(2).visgrid.get(2), 0.1);
	}
	
	@Test
	public void testNoOverExpansion() {
		
		/**
		 * 	verify first row will not "overexpand" to go beyond beginning
		 * 	of 2nd cell next row 
		 * 	oooo	|	 oooo
		 *  ooo		| ooooooooo
		 */
		DocumentTable tab = new DocumentTable();
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.get(0).add(new TableCell(10,10,50,20));
		tab.trs.get(0).add(new TableCell(200,10,220,20));
		tab.trs.get(1).add(new TableCell(10,30,45,40));
		tab.trs.get(1).add(new TableCell(100,30,145,40));
		
		tab.calculateGridLocation();
		tab.maximizeCells();
		
		org.junit.Assert.assertTrue( tab.trs.get(0).get(0).visgrid.get(2) <= 100.0);
		org.junit.Assert.assertEquals(75.0f,  (float) tab.trs.get(0).get(1).visgrid.get(0), 0.1);
		
		org.junit.Assert.assertTrue( tab.trs.get(1).get(0).visgrid.get(2) <= 100.0);
		org.junit.Assert.assertEquals(75.0f,  (float) tab.trs.get(1).get(1).visgrid.get(0), 0.1);
		
	}
	
	@Test
	public void testVerticalExpansion() {
		DocumentTable tab = new DocumentTable();
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.get(0).add(new TableCell(10,10,50,50));
		tab.trs.get(0).add(new TableCell(200,10,220,20));
		tab.trs.get(0).add(new TableCell());
		tab.trs.get(1).add(new TableCell(13,60,45,142));
		tab.trs.get(1).add(new TableCell(100,75,145,90));
		tab.trs.get(1).add(new TableCell(240,75,255,95));
		
		tab.calculateGridLocation();
		tab.maximizeCells();	
		
		// top val first row
		org.junit.Assert.assertEquals(10.0f,  (float) tab.trs.get(0).get(0).visgrid.get(1), 0.1);
		org.junit.Assert.assertEquals(10.0f,  (float) tab.trs.get(0).get(1).visgrid.get(1), 0.1);
		org.junit.Assert.assertEquals(10.0f,  (float) tab.trs.get(0).get(2).visgrid.get(1), 0.1);
		
		// bottom first, top second
		org.junit.Assert.assertEquals(55.0f,  (float) tab.trs.get(0).get(0).visgrid.get(3), 0.1);
		org.junit.Assert.assertEquals(55.0f,  (float) tab.trs.get(0).get(1).visgrid.get(3), 0.1);
		org.junit.Assert.assertEquals(55.0f,  (float) tab.trs.get(1).get(0).visgrid.get(1), 0.1);
		org.junit.Assert.assertEquals(55.0f,  (float) tab.trs.get(1).get(1).visgrid.get(1), 0.1);
		
		// bottom second row
		org.junit.Assert.assertEquals(142.0f,  (float) tab.trs.get(1).get(0).visgrid.get(3), 0.1);
		org.junit.Assert.assertEquals(142.0f,  (float) tab.trs.get(1).get(1).visgrid.get(3), 0.1);
	}
	
	@Test
	public void testExpansionIsIdempotent() {
		// multiple calls to expansion must yield convergent cell dimensions
		
		DocumentTable tab = new DocumentTable();
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		tab.trs.add(new ArrayList<TableCell>());
		
		tab.trs.get(0).add(new TableCell(10,10,20,20));
		tab.trs.get(0).add(new TableCell(200,12,220,18));
		
		tab.trs.get(1).add(new TableCell(5,70,25,90));
		tab.trs.get(1).add(new TableCell(180,70,225,100));
		
		tab.trs.get(2).add(new TableCell(10,120,30,130));
		tab.trs.get(2).add(new TableCell(200,120,225,130));
		
		tab.calculateGridLocation();
		
		
		for (int i = 0 ; i < 20; i++) {
			tab.maximizeCells();
			org.junit.Assert.assertEquals(105.0f,  (float) tab.trs.get(1).get(0).visgrid.get(2), 0.1);
			org.junit.Assert.assertEquals(130.0f,  (float) tab.trs.get(2).get(0).visgrid.get(3), 0.1);
		}
	}
}
