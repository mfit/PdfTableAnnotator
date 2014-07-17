package at.tugraz.kti.pdftable.document.export;

import java.io.File;
import java.io.FileNotFoundException;

import at.tugraz.kti.pdftable.document.RichDocument;

public class Export {
	String format = "plain";
	RichDocument doc;
	
	public String getMimeType() {
		return "text/" + format;
	}
	
	public void setDocument(RichDocument d) {
		doc = d;
	}
	
	public void export() throws ExportException {
		
	}
	
	public String getExportFilename(String docname) {
		return docname + "." + getFormat();
	}
	
	public String getFormat() {
		return format;
	}
	
	public String toString() {
		return "";
	}
	
	public void toFile(File outfile) throws  FileNotFoundException, Exception {
		// new FileOutputStream(outfile);
	}
	
	public static Export factory(String export_name) {
		Export exp = null;
		if (export_name.toLowerCase().equals("html")) 
			exp = new HtmlExport();
		else if (export_name.toLowerCase().equals("structure"))
			exp = new StructureExport();
		else if (export_name.toLowerCase().equals("region"))
			exp = new RegionExport();
		else if (export_name.toLowerCase().equals("logical")
				|| export_name.toLowerCase().equals("functional"))
			exp = new LogicalStructureExport();
		else if (export_name.toLowerCase().equals("csv"))
			exp = new CsvExport();
		
		return exp;
	}
}
