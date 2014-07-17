package at.tugraz.kti.pdftable.data;

public class RepositoryException extends Exception {
	public RepositoryException(String string) {
		super(string);
	}
	
	public RepositoryException(Exception e) {
		super(e);
	}
}
