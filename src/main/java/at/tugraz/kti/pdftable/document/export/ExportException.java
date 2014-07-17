package at.tugraz.kti.pdftable.document.export;

public class ExportException extends Exception {
	public ExportException(String string) {
		super(string);
	}
	
	public ExportException(Exception e) {
		super(e);
	}
}
