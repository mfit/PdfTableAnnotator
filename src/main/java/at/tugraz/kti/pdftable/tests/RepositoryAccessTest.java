package at.tugraz.kti.pdftable.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import at.tugraz.kti.pdftable.data.RepositoryAccess;
import at.tugraz.kti.pdftable.data.RepositoryException;
import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.DocumentTables;

public class RepositoryAccessTest {
	
	@Test
	public void testFileIO() throws RepositoryException, IOException {
		File file = new File("resources/tests/testout.json");
		
		DocumentTables tdefs = new DocumentTables();
		DocumentTable table = new DocumentTable();
		table.trs.add(new ArrayList<TableCell>());
		table.trs.get(0).add(new TableCell(40,40,100,50));
		tdefs.addTable(0,table);
		
		RepositoryAccess.saveTableDefinitions(file, tdefs);
		tdefs = RepositoryAccess.loadTableDefinitions(file);
		
		TableCell loaded = tdefs.annotationsOnPage.get(0).get(0).trs.get(0).get(0);
		org.junit.Assert.assertEquals((double)40, (double)loaded.visgrid.get(0), 0.01);
	}
	
	@Test
	public void testLoadTableDefinitions() throws JsonParseException, JsonMappingException, IOException {
		File f = new File("resources/tests/annotation_data.json");
		//f = new File("resources/tests/testout.json");
		
		DocumentTables tdefs = RepositoryAccess.loadTableDefinitions(f);
		System.out.println(tdefs.getTables().size());
	}
	
	/**
	 * Test repository access can be instantiated and that some documents
	 * are retrieved from the index and that the first document has some filename.
	 * @throws RepositoryException
	 * @throws IOException
	 */
	@Test
	public void testImportAndAccess() throws RepositoryException, IOException {
		RepositoryAccess ra = RepositoryAccess.getInstance("resources/repos/test", "default");
		
		HashMap<Integer, String> documents = ra.getDocuments();
		org.junit.Assert.assertTrue(documents.size() > 0);
		
		RichDocument doc = ra.getDocumentById(1);
		org.junit.Assert.assertTrue(doc.getFilename().length() > 4);
		
	}
	
	@Test
	public void testFindRelatedFile() throws RepositoryException, IOException {
		RepositoryAccess ra = RepositoryAccess.getInstance("resources/repos/test", "default");
		HashMap<Integer, String> documents = ra.getDocuments();
		RichDocument doc = ra.getDocumentById(1);
		File f = doc.getSourcePath();
		int pos = f.getName().lastIndexOf(".");
		String trunc = pos > 0 ? f.getName().substring(0, pos) : f.getName();
		org.junit.Assert.assertEquals("01227792", trunc);
	}

}
