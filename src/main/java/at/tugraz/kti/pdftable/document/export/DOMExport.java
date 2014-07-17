package at.tugraz.kti.pdftable.document.export;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.DocumentTables;

/**
 * Creates tables-export of a document in HTML format
 */
public class DOMExport extends Export {
	
	protected Document dom;
	protected String format;
	
	public DOMExport() {
		super();
		dom = null;
		format = "xml";
	}
	
	
	public void export() throws ExportException {
		dom = export(doc, doc.getTableAnnotations());
	}
	
	/**
	 * export 
	 * uses subclass' buildDom() method to prepare the actual DOM
	 * @param doc
	 * @param tdef
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document export(RichDocument doc, DocumentTables tdef) throws ExportException {
		try {
			initExportDom();
			buildDom(doc, tdef);
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new ExportException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new ExportException(e.getMessage());
		}
		return dom;
	}
	
	public String getMimeType() {
		return "text/" + format;
	}
	
	public String getExportFilename(String docname) {
		return docname + "." + getFormat();
	}
	
	public String getFormat() {
		return format;
	}
	
	/**
	 * set up dom transformer
	 *  
	 * @return
	 * @throws ExportException 
	 * @throws TransformerConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 */
	public Transformer getDefaultTransformer() throws ExportException {
		Transformer tr = null;
		try {
			tr = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new ExportException(e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			throw new ExportException(e.getMessage());
		}
		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		tr.setOutputProperty(OutputKeys.METHOD, format);
		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		return tr;
	}
	
	/**
	 * dom transformed as string
	 * @throws ExportException 
	 */
	public String toString() {
		Transformer tr;
		StringWriter writer = new StringWriter();
		try {
			tr = getDefaultTransformer();
			tr.transform(new DOMSource(dom), new StreamResult(writer));
		} catch (ExportException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		return writer.getBuffer().toString();
	}
	
	/**
	 * dom transformed to file
	 * @param outfile
	 * @throws TransformerFactoryConfigurationError
	 * @throws FileNotFoundException
	 * @throws TransformerException
	 */
	public void toFile(File outfile) throws  FileNotFoundException, ExportException {
		try {
			Transformer tr = getDefaultTransformer();
			tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(outfile)));
		} catch (TransformerFactoryConfigurationError e) {
			throw new ExportException(e.getMessage());
		} catch (TransformerException e) {
			throw new ExportException(e.getMessage());
		}
	}
	
	/**
	 * initialize dom 
	 * @throws ParserConfigurationException
	 */
	protected void initExportDom() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// dbf.setNamespaceAware(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		dom = db.newDocument();
	}
	
	/**
	 * override .. 
	 * @throws ParserConfigurationException 
	 * @throws DocumentException 
	 */
	protected void buildDom(RichDocument doc, DocumentTables tdef) throws ParserConfigurationException, DocumentException { }
}
