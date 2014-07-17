package at.tugraz.kti.pdftable.document;

import java.util.ArrayList;

/**
 * To encode/decode JSON-representation.
 * Instances of this class are received serialized from the front-end/clients. 
 * @author "matthias frey"
 */
public class TableModel {
	public String id;
	public ArrayList<ArrayList<TableCell>> trs;
}
