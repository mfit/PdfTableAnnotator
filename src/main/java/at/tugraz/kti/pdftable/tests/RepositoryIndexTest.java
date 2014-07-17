package at.tugraz.kti.pdftable.tests;

import java.io.File;
import static org.junit.Assert.fail;
import org.junit.Test;

import at.tugraz.kti.pdftable.data.RepositoryException;


import at.tugraz.kti.pdftable.data.DocumentIndex;

public class RepositoryIndexTest {
	
	@Test
	public void testInitDocumentIndex() throws RepositoryException {
		File f = new File("resources/tests/test.json");
		if (f.exists()) f.delete();
		if (f.exists()) fail("Cannot set up test data.");
		
		DocumentIndex di = new DocumentIndex("resources/tests/test.json");
		di.init();
		org.junit.Assert.assertTrue("Index should be created", f.exists());
	}
	
	@Test
	public void testAddRetrieveDocumentIndex() throws RepositoryException {
		File f = new File("resources/tests/test.json");
		if (f.exists()) f.delete();
		if (f.exists()) fail("Cannot set up test data.");
		
		DocumentIndex di = new DocumentIndex("resources/tests/test.json");
		di.init();
		di.add(new File("resources/tests/file1.pdf"));
		org.junit.Assert.assertEquals("file1.pdf", di.getDocumentById(1).getName());
		
		di.add(new File("resources/tests/file2.pdf"));
		org.junit.Assert.assertEquals("file1.pdf", di.getDocumentById(1).getName());
		org.junit.Assert.assertEquals("file2.pdf", di.getDocumentById(2).getName());
	}
	
	@Test
	public void testDuplicateCheck() throws RepositoryException {
		File f = new File("resources/tests/test.json");
		if (f.exists()) f.delete();
		if (f.exists()) fail("Cannot set up test data.");
		DocumentIndex di = new DocumentIndex("resources/tests/test.json");
		
		di.init();
		di.add(new File("resources/tests/file1.pdf"));
		di.add(new File("resources/tests/new/file1.pdf"));
		
		boolean exc = false;
		try {
			di.add(new File("resources/tests/file1.pdf"));
		} catch(RepositoryException e) {
			exc = true;
		}
		
		org.junit.Assert.assertTrue("Duplicate exception should have occured", exc);
		
		di.add(new File("resources/tests2/file1.pdf"));
		
		org.junit.Assert.assertEquals("resources/tests/file1.pdf", 
				di.getDocumentById(1).toString());
		org.junit.Assert.assertEquals("resources/tests/new/file1.pdf", 
				di.getDocumentById(2).toString());
		org.junit.Assert.assertEquals("resources/tests2/file1.pdf", 
				di.getDocumentById(3).toString());
	}
	
	@Test
	public void testAbsoluteRepositoryPath() throws RepositoryException {
		File f = new File("resources/tests/test.json");
		if (f.exists()) f.delete();
		if (f.exists()) fail("Cannot set up test data.");
		DocumentIndex di = new DocumentIndex("resources/tests/test.json", 
				"/my/abs/dir");
		di.init();
		di.add(new File("hello/file.pdf"));
		org.junit.Assert.assertEquals("/my/abs/dir/hello/file.pdf", 
				di.getDocumentById(1).getAbsolutePath());
		
		
	}
	
}
