package at.tugraz.kti.pdftable.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.jetty.util.log.LoggerLog;
import org.xml.sax.SAXException;

import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.TableModel;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.document.export.ExportException;
import at.tugraz.kti.pdftable.document.export.Importer;
import at.tugraz.kti.pdftable.document.export.StructureExport;
import at.tugraz.kti.pdftable.extract.BlockInfo;
import at.tugraz.kti.pdftable.extract.TableBoundaryClassifier;
import at.tugraz.kti.pdftable.extract.WordExtractor;

/**
 * Manage data (source-files, annotations). Works on a directory where
 * source-files and annotations are stored.
 * 
 * A JSON index of all documents is kept an managed.
 * 
 * Annotations are stored with regard to a "working set", which can be simply a
 * user id or user name, so different users/groups can work on separate 
 * annotation sets.
 * 
 * @author "matthias frey"
 */
public class RepositoryAccess {

	protected DocumentIndex index;

	protected Properties properties;
	protected String workingSet = "default";
	protected String srcpath;
	protected String datapath;
	protected String tablesFilename = "tables_v2.json";
	protected String repositoryName;
	public boolean externalMode = false;
	
	public static final String EXTERNAL_MODE = "__external__";

	protected Logger logger;

	public RepositoryAccess(String dir, String working_set) {
		init(dir, working_set);
		properties = new Properties();
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setOptions(String basedir, String index, String datadir) {
		srcpath = basedir;
		datapath = datadir;
		this.index = new DocumentIndex(index, basedir);
		checkRepo();
	}

	protected void init(String dir, String working_set) {
		File f = new File(dir);
		repositoryName = f.getName();
		workingSet = working_set;

		srcpath = dir + "/src";
		datapath = dir + "/data";
		
		index = new DocumentIndex(srcpath + "/documents.json", srcpath);
		// checkRepo();
		
		externalMode = false;
	}

	public static RepositoryAccess getInstance(String repo, String wset) {
		return new RepositoryAccess(repo, wset);
	}

	public void setProperties(Properties prop) {
		properties = prop;
	}
	
	public String getRepositoryName() {
		return externalMode ? 
				RepositoryAccess.EXTERNAL_MODE : repositoryName;
	}
	
	public String getWorkingSet() {
		return workingSet;
	}

	/**
	 * Test if repository directory structure is present and usable.
	 * If not, attempt to create and initialise.
	 */
	public boolean checkRepo() {
		File datadir = new File(datapath);
		
		File basedir = new File(datadir.getParent());
		if (!basedir.isDirectory()) {
			basedir.mkdir();
		}
		
		if (!datadir.isDirectory() && !datadir.mkdir()) {
			throw new RuntimeException(datadir.getAbsolutePath()
					+ " could not be created");
		}
		File srcrdir = new File(srcpath);
		if (!srcrdir.isDirectory() && !srcrdir.mkdir()) {
			throw new RuntimeException(srcrdir.getAbsolutePath()
					+ " could not be created");
		}

		try {
			index.init();
		} catch (RepositoryException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * return id/names of all documents in the repository
	 * 
	 * @return HashMap<Integer, String> document name and id
	 * @throws RepositoryException
	 */
	public HashMap<Integer, String> getDocuments() throws RepositoryException {
		return index.getDocuments();
	}
	
	/**
	 * Load and return document as a RichDocument object (by id). Document is
	 * opened an parsed, the annotated tables are added.
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public RichDocument getDocumentById(int id) throws RepositoryException,
			IOException {
		File f = index.getDocumentById(id);
		if (f == null) {
			return null;
		}
		String filename = f.getAbsolutePath();
		RichDocument d = new RichDocument(filename);
		d.open();
		d.setTableAnnotations(getAnnotatedTables(id));
		return d;
	}

	public String getSourceDirectory() {
		return index.getBasePath().toString();
		// return srcpath;
	}

	/**
	 * Add (or update) a table annotation to/in a document's page
	 * 
	 * @param docid
	 * @param pagen
	 * @param tabledef
	 *            as string in json format
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void addTableToDocument(int docid, int pagen,
			TableModel tabledef) throws JsonParseException,
			JsonMappingException, IOException, RepositoryException {

		// instantiate doc to validate docid
		RichDocument doc = getDocumentById(docid);

		// Load the set of tables from file
		File file = new File(_getDataDir(docid), tablesFilename);
		DocumentTables tdefs = loadTableDefinitions(file);

		// Create entry page if non-existent
		if (!tdefs.annotationsOnPage.containsKey(pagen)) {
			tdefs.annotationsOnPage.put(pagen, new ArrayList<DocumentTable>());
		}

		// Set up table instance.
		// Try to load by id if set, otherwise create new.
		int table_id = Integer.parseInt(tabledef.id);
		DocumentTable dt = null;
		if (table_id != 0) {
			dt = tdefs.getTable(table_id);
		}
		if (dt == null) {
			dt = DocumentTable.createTable();
			tdefs.annotationsOnPage.get(pagen).add(dt);
		}

		// Set incoming data (pagenumber, cells) to table.
		dt.page = pagen;
		dt.trs = tabledef.trs;

		// Save tables to file.
		saveTableDefinitions(file, tdefs);

		if (properties.getProperty("autosave-export", "") != "") {
			// Update doc before saving auto export.
			doc.setTableAnnotations(tdefs);
			_handleAutoExports(doc);
		}
	}

	/**
	 * Load a DocumentTables data-structure from a JSON file.
	 * 
	 * @param file
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static DocumentTables loadTableDefinitions(File file)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DocumentTables tdefs = new DocumentTables();
		if (file.isFile()) {
			tdefs = mapper.readValue(file, DocumentTables.class);
		}
		return tdefs;
	}

	/**
	 * Save DocumentTables data-structure to disk as JSON.
	 * 
	 * @param file
	 * @param tdefs
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static void saveTableDefinitions(File file, DocumentTables tdefs)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.writeValue(file, tdefs);
	}

	/**
	 * Load the definitions of annotated tables that are stored for a document.
	 * 
	 * 
	 * @param docid
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public DocumentTables getAnnotatedTables(int docid)
			throws JsonParseException, JsonMappingException, IOException {
		
		File file = new File(_getDataDir(docid), tablesFilename);
		DocumentTables tdefs = loadTableDefinitions(file);
		return tdefs;
	}

	public ArrayList<DocumentTable> getAnnotatedTables(int docid, int pageid)
			throws JsonParseException, JsonMappingException, IOException {
		DocumentTables tdefs = getAnnotatedTables(docid);
		if (tdefs.annotationsOnPage.containsKey(pageid)) {
			return tdefs.annotationsOnPage.get(pageid);
		} else {
			return new ArrayList<DocumentTable>();
		}
	}

	/**
	 * Delete a table-annotation, by table id, page and document-id.
	 * 
	 * @param docid
	 * @param pagen
	 * @param tableid
	 * @throws IOException
	 */
	public void clearTable(int docid, int pagen, int tableid)
			throws IOException {
		File file = new File(_getDataDir(docid), tablesFilename);
		DocumentTables tdefs = loadTableDefinitions(file);
		tdefs.removeTable(pagen, tableid);
		saveTableDefinitions(file, tdefs);
	}

	/**
	 * Clear all annotations for a document.
	 * 
	 * @param docid
	 */
	public void resetAnnotations(int docid) throws IOException {
		File file = new File(_getDataDir(docid), tablesFilename);
		DocumentTables tdefs = new DocumentTables();
		saveTableDefinitions(file, tdefs);
	}

	/**
	 * Clear annotations for a whole document/page.
	 * 
	 * @param docid
	 * @param pagen
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void clearAnnotations(int docid, int pagen)
			throws JsonParseException, JsonMappingException, IOException {
		File file = new File(_getDataDir(docid), tablesFilename);
		DocumentTables tdefs = loadTableDefinitions(file);
		if (tdefs.annotationsOnPage.containsKey(pagen)) {
			tdefs.annotationsOnPage.remove(pagen);
		}
		saveTableDefinitions(file, tdefs);
	}

	/**
	 * Retrieve data of some model, classifier or preprocessing step regarding a
	 * document. Data is returned in JSON format (no strict types,
	 * interoperability) .
	 * 
	 * @param docid
	 * @param what
	 * @param pagen
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public String getDataLayerAsJson(int docid, int pagen, String what)
			throws RepositoryException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		// instantiate doc to validate docid
		RichDocument doc = getDocumentById(docid);

		if (what.equals("chars")) {

			return mapper.writeValueAsString(doc.charactersOnPage.get(pagen));

		} else if (what.equals("words")) {

			ArrayList<ArrayList<BlockInfo>> blockinfo = null;

			WordExtractor we = new WordExtractor();
			we.setCharacters(doc.charactersOnPage.get(pagen));
			we.extractWords();
			return mapper.writeValueAsString(we.getWords());

		} else if (what.equals("table")) {
			// Try to find sparse lines by using
			// the line detection of WordExtractor,
			// then TableBoundaryClassifier
			// (experimental / very basic).

			WordExtractor we = new WordExtractor();
			we.setCharacters(doc.charactersOnPage.get(pagen));
			we.extractWords();
			TableBoundaryClassifier tbc = new TableBoundaryClassifier();
			tbc.setLines(we.getLines());
			tbc.classifyLines();

			ArrayList<BlockInfo> myLines = new ArrayList<BlockInfo>();
			for (ArrayList<BlockInfo> raw_line : tbc.getTableLines()) {
				BlockInfo curline = new BlockInfo();
				for (BlockInfo b : raw_line) {
					curline.addBlock(b);
				}
				myLines.add(curline);
			}

			return mapper.writeValueAsString(myLines);
		} else if (what.equals("import")) {

			// Find/guess structure file
			File file = _findExternalStructFile(doc);
			if (file != null) {
				// Found, import external structure file.
				Importer imp = new Importer();
				DocumentTables tdefs = null;
				try {
					tdefs = imp.getTableDefinitions(file, doc);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}
				if (tdefs.annotationsOnPage.containsKey(pagen)) {
					return mapper.writeValueAsString(tdefs.annotationsOnPage.get(pagen));
				} else {
					// no tables for page
					return "[]";
				}
			} else {
				// no structure file found
				return "[]";
			}
		} else {
			return "";
		}
	}

	/**
	 * Add a document to the repository (by filename) . Throws IOException in
	 * case of invalid file (no pdf) or duplicate file. Copy the file into
	 * repository.
	 * 
	 * @param doc
	 * @throws IOException
	 * @throws RepositoryException
	 */
	public void addDocument(File doc) throws IOException, RepositoryException {
		File importfile = new File(srcpath + '/' + doc.getName());
		FileUtils.copyFile(doc, importfile); // or doc.renameTo(..);

		// Open to validate
		PDDocument document = PDDocument.load(doc.getAbsolutePath());
		document.close();

		// Add to index
		indexDocument(new File(doc.getName()));

	}

	/**
	 * Add a document to index ( but do not copy or move file ) .
	 * 
	 * @param doc
	 * @throws RepositoryException
	 */
	public int indexDocument(File doc) throws RepositoryException {
		return index.add(doc);
	}

	public DocumentIndex getDocumentIndex() {
		return index;
	}

	public Logger getLogger() {
		return logger;
	}

	protected File _findExternalStructFile(RichDocument doc) {
		File source = doc.getSourcePath();
		int pos = source.getName().lastIndexOf(".");
		String trunc_name = pos > 0 ? source.getName().substring(0, pos)
				: source.getName();
		File file;

		file = new File(source.getAbsolutePath() + "-str.xml");
		if (file.isFile()) {
			return file;
		}

		file = new File(source.getParent(), trunc_name + "-str.xml");
		if (file.isFile()) {
			return file;
		}

		file = new File(properties.getProperty("external-datadir", ""),
				doc.getFilename() + "-str.xml");
		if (file.isFile()) {
			return file;
		}

		file = new File(properties.getProperty("external-datadir", ""),
				trunc_name + "-str.xml");
		if (file.isFile()) {
			return file;
		}

		return null;
	}

	/**
	 * A helper to retrieve (and possible create beforehand) the directory for
	 * storing annotation information for a document.
	 * 
	 * @param docid
	 * @return
	 */
	protected File _getDataDir(int docid) {
		File datadir = new File(datapath);
		if (!datadir.isDirectory() && !datadir.mkdir()) {
			throw new RuntimeException(datadir.getAbsolutePath()
					+ " could not be created");
		}
		
		File working_setdir = new File(datapath, workingSet);
		if (!working_setdir.isDirectory() && !working_setdir.mkdir()) {
			throw new RuntimeException(datadir.getAbsolutePath()
					+ " could not be created");
		}

		File dir = new File(working_setdir, String.valueOf(docid));
		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new RuntimeException(dir.getAbsolutePath()
					+ " could not be created");
		}
		return dir;
	}

	public void writeExports(RichDocument doc) throws JsonGenerationException,
			JsonMappingException, ExportException,
			IOException {
		
		File parent = new File(datapath, "exports");
		
		if (!parent.isDirectory()) {
			parent.mkdir();
		}
		writeExports(parent, doc);
	}

	public void writeExports(File parent, RichDocument doc)
			throws ExportException, JsonGenerationException, JsonMappingException, IOException {
		File source = doc.getSourcePath();
		int pos = source.getName().lastIndexOf(".");
		String trunc_name = pos > 0 ? source.getName().substring(0, pos)
				: source.getName();

		// Save structure export.
		StructureExport structure_exp = new StructureExport();
		structure_exp.export(doc, doc.getTableAnnotations());
		structure_exp.toFile(new File(parent, trunc_name + "-str.xml"));

//		// Save JSON.
//		saveTableDefinitions(new File(parent, trunc_name + ".json"),
//				doc.getTableAnnotations());
	}

	/**
	 * Handle "auto Exports" , i.e. saving an export whenever incoming table
	 * data is stored to disk internally.
	 * 
	 * @param doc
	 */
	protected void _handleAutoExports(RichDocument doc) {

		// Determine parent directory.
		File parent = new File(doc.getSourcePath().getParent());
		if (properties.getProperty("autosave-target", "") != "") {
			parent = new File(properties.getProperty("autosave-target"));
		}
		try {
			writeExports(parent, doc);

		} catch (Exception e) {
			System.out.println("Failed to write auto-exports...");
			e.printStackTrace();
		}
	}
}
