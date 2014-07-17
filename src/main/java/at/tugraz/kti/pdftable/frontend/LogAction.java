package at.tugraz.kti.pdftable.frontend;

/**
 * Class that helps with serializing a log entry representing an action.
 * @author "matthias frey"
 */
public class LogAction {
	public LogAction(String session, String filename, int docid, int pageid, 
			int tableid, String action, String json) {
		this.session = session;
		this.filename = filename;
		this.docid = docid;
		this.pageid = pageid;
		this.tableid = tableid;
		this.action = action;
		this.json = json;
	}
	public String session;
	public String filename;
	public int docid;
	public int pageid;
	public int tableid;
	public String action;
	public String json;
	
	public String toString() {
		return this.session + ";" 
				+ this.filename + ";"
				+ this.docid + ";"
				+ this.pageid + ";"
				+ this.tableid + ";"
				+ this.action + ";"
				+ this.json;
	}
}