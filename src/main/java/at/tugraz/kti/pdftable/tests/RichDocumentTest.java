package at.tugraz.kti.pdftable.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.extract.CharInfo;

public class RichDocumentTest {

	@Test
	public void test() throws DocumentException {
		RichDocument doc = new RichDocument(
				"src/main/java/at/tugraz/kti/pdftable/tests/" +
				"position-test.pdf");
		try {
			doc.open();
			ArrayList<CharInfo> chars = doc.getCharactersInBoundingBox(0,  50, 60, 70, 80);
			assertEquals("A", chars.get(0).c);
		} catch ( IOException e) {
			fail(e.getMessage());
		}
	}

}
