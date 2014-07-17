package at.tugraz.kti.pdftable.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.document.export.Importer;

public class ImporterTest {

//	@Test
//	public void testImport() throws ParserConfigurationException, SAXException, IOException {
//		File f = new File("resources/tests/structure2.xml");
//		Importer im = new Importer();
//		DocumentTables tabs = im.getTableDefinitions(f);
//		DocumentTable tab = tabs.pages.get(0).get(0);
//	}

	

	@Test
	public void testFullImport() throws ParserConfigurationException, SAXException, IOException {
		File f = new File("resources/tests/us-012.pdf-struct-orig-edited.xml");
		Importer im = new Importer();
		DocumentTables tabs = im.getTableDefinitions(f);
		
		//DocumentTable tab = tabs.pages.get(2).get(0);
	}
	
	@Test
	public void testImportAndExpansion() throws ParserConfigurationException, SAXException, IOException {
		File f = new File("resources/tests/us-012.pdf-struct.xml");
		Importer im = new Importer();
		DocumentTables tabs = im.getTableDefinitions(f);
		DocumentTable tab = tabs.annotationsOnPage.get(2).get(0);
		
		//
		// assert approx. positions of some cells
		//
		
		// left x pos
		assertEquals(230.0, tab.trs.get(0).get(0).visgrid.get(0), 10.0);
		assertEquals(230.0, tab.trs.get(1).get(0).visgrid.get(0), 10.0);
		assertEquals(230.0, tab.trs.get(2).get(0).visgrid.get(0), 10.0);
		// ...
		
		assertEquals(298.0, tab.trs.get(0).get(1).visgrid.get(0), 10.0);
		assertEquals(298.0, tab.trs.get(1).get(1).visgrid.get(0), 10.0);
		assertEquals(298.0, tab.trs.get(2).get(1).visgrid.get(0), 10.0);
		// ...
		
		//assertEquals(519.0, tab.trs.get(0).get(5).visgrid.get(2), 10.0);
		assertEquals(564.0, tab.trs.get(0).get(4).visgrid.get(2), 10.0);
		
		// top y pos
		// assertEquals(127.0, tab.trs.get(0).get(0).visgrid.get(1), 10.0);
		
		
	}
	
	

}
