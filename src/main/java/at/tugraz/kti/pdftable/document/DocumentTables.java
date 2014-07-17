package at.tugraz.kti.pdftable.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;


/**
 * Represents sets of tables (tables of a document) .
 * Tables can be retrieved as lists, either for a single page or whole document. 
 */
public class DocumentTables {
	
	public HashMap<Integer, ArrayList<DocumentTable>> annotationsOnPage;
	
	public DocumentTables() {
		annotationsOnPage = new HashMap<Integer, ArrayList<DocumentTable>>();
	}
	
	/**
	 * Remove a table by specifying page number and table id.
	 * @param pagen
	 * @param tid
	 */
	public void removeTable(int pagen, int tid) {
		if (annotationsOnPage.containsKey(pagen)) {
			for (DocumentTable tab: annotationsOnPage.get(pagen)) {
				if (tab.tn == tid) {
					annotationsOnPage.get(pagen).remove(tab);
					break;
				}
			}
		}
	}
	
	/**
	 * Add a DocumentTable. Table will be added to the page that is specified
	 * with the table.
	 * @param tab
	 */
	public void addTable(DocumentTable tab) {
		addTable(tab.page, tab);
	}
	
	/**
	 * Add a DocumentTable, to a specific page number. That page number will
	 * also be set to the table.
	 * @param pagen
	 * @param tab
	 */
	public void addTable(int pagen, DocumentTable tab) {
		if (!annotationsOnPage.containsKey(pagen)) {
			annotationsOnPage.put(pagen, new ArrayList<DocumentTable>());
		}
		tab.page = pagen;
		annotationsOnPage.get(pagen).add(tab);
	}
	
	/**
	 * Get a table (table definitions) by its numeric id. 
	 * @param tid
	 * @return
	 */
	@JsonIgnore
	public DocumentTable getTable(int tid) {
		for(int pagen : annotationsOnPage.keySet()) {
			for (DocumentTable tab: annotationsOnPage.get(pagen)) {
				if (tab.tn == tid) {
					return tab;
				}
			}
		}
		return null;
	}

	/**
	 * Get all tables on a page. 
	 * @param tid
	 * @return
	 */
	@JsonIgnore
	public ArrayList<DocumentTable> getTables(int page) {
		if (annotationsOnPage.containsKey(page)) {
			return annotationsOnPage.get(page);
		} else {
			return new ArrayList<DocumentTable>();
		}
	}

	@JsonIgnore
	public ArrayList<DocumentTable> getTables() {
		ArrayList<DocumentTable> all = new ArrayList<DocumentTable>();
		for ( Map.Entry <Integer, ArrayList<DocumentTable>> pageEntry : annotationsOnPage.entrySet()) {
			all.addAll(pageEntry.getValue());
		}
		return all;
	}
}
