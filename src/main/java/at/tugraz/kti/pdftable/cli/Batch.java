package at.tugraz.kti.pdftable.cli;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.export.CsvExport;
import at.tugraz.kti.pdftable.document.export.Export;
import at.tugraz.kti.pdftable.document.export.ExportException;
import at.tugraz.kti.pdftable.document.export.Importer;
import at.tugraz.kti.pdftable.frontend.App;
import at.tugraz.kti.pdftable.data.RepositoryAccess;
import at.tugraz.kti.pdftable.data.RepositoryException;

/**
 * Command-line user interface.
 * You can:
 * 	- import directories
 *  - examine a repository / working-set
 *  - create exports of single files
 *  - create exports of all the documents in a repository / working-set 
 * @author "matthias frey"
 */
public class Batch {

	/**
	 * import pdf documents from a directory into repository
	 * @param dir
	 * @throws IOException
	 * @throws RepositoryException 
	 */
	public void importBatch(String dir, String repository) throws IOException, RepositoryException {
		String reponame 	= repository;
		String workingset 	= "default";
		App app = App.getInstance();
		RepositoryAccess repo = app.getRepositoryAccess(reponame, workingset);
		repo.checkRepo();
		
		File imports = new File(dir);
		if ( !imports.isDirectory()) 
			throw new RuntimeException("Import directory not found");
		Collection<String> existing_docs = repo.getDocuments().values();
		for (String fname : imports.list()) {
			File doc = new File(dir, fname);
			if (doc.isFile()) {
				System.out.print("\n" + doc.getName() + "...");
				if (!existing_docs.contains(doc.getName())) {
					if(doc.getName().endsWith(".pdf")) {
						System.out.print("imported");
						repo.addDocument(doc);
					} else {
						System.out.print("ignored");
					}
				} else {
					System.out.print("skipped (duplicate)");
				}
			}
		}
	}
	
	/**
	 * @param repository
	 * @param working_set
	 * @param id
	 * @param export_type
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws RepositoryException
	 * @throws ExportException 
	 */
	public void exportDocument(String repository, String working_set, int id, 
			String export_type) throws IOException, ParserConfigurationException, 
			RepositoryException, ExportException {
		
		App app = App.getInstance();
		RepositoryAccess repo = app.getRepositoryAccess(repository, working_set);
		
		RichDocument doc = repo.getDocumentById(id);		
		// Export export = new LogicalStructureExport();
		Export export = Export.factory(export_type);
		export.setDocument(doc);
		export.export();
		System.out.println(export.toString());
	}
	
	/**
	 * Print information about a specific repository / working_set (document
	 * index contents and number of tables annotated for each document) .
	 *  
	 * @param repository
	 * @param working_set
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws RepositoryException
	 */
	public void printRepositoryInfo(String repository, String working_set) 
			throws IOException, ParserConfigurationException, 
			RepositoryException {
		App app = App.getInstance();
		RepositoryAccess repo = app.getRepositoryAccess(repository, working_set);
		
		System.out.println("Showing " + repo.getRepositoryName() + 
				"/" +  repo.getWorkingSet() + "...");
		
		for (Map.Entry<Integer, String> entry : repo.getDocuments().entrySet()) {
			RichDocument doc = repo.getDocumentById(entry.getKey());
			int n_tables = doc.getTableAnnotations().getTables().size();
			System.out.println(entry.getKey() + "\t" + entry.getValue() 
					+ "\t" + n_tables);
		}
	}
	
	public void convertToCsv(String structurefile, String pdfsrc) 
			throws Exception {
		
		RichDocument doc = new RichDocument(pdfsrc);
		doc.open();
		File f = new File(structurefile);
		Importer importer = new Importer();
		DocumentTables tables = importer.getTableDefinitions(f, doc);
		doc.setTableAnnotations(tables);
		
		Export csv = new CsvExport();
		csv.setDocument(doc);
		csv.toFile(new File(f.getName() + "-converted.csv"));
		//System.out.println(csv.toString());
		
	}
	
	/**
	 * DOMExport annotation files for all documents in repository.
	 * 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws Exception 
	 */
	public void exportAll(String repository, String working_set, String export_type) 
		throws TransformerFactoryConfigurationError, Exception {
		
		// Instantiate repository with working set
		App app = App.getInstance();
		RepositoryAccess repo = app.getRepositoryAccess(repository, working_set);
		Export export = Export.factory(export_type);

		// Verify / create destination directory
		File theDir = new File("resources/results/" + repository + "-" + working_set);
		if (!theDir.exists()) {
			theDir.mkdir();
		}
		
		// Export 
		for(int id : repo.getDocuments().keySet()) {
			RichDocument doc = repo.getDocumentById(id);
			export.setDocument(doc);
			export.export();
			String filename = doc.getFilename();
			filename = filename.substring(0, filename.lastIndexOf('.'));
			File outfile = new File(theDir, export.getExportFilename(filename));
			export.toFile(outfile);
			System.out.println("Exported " + outfile);			
		}
	}
	
	/**
	 * Static entry point for command line script.
	 * Arguments are parsed here, once action is determined the suitable
	 * method is called to actually perform the action.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Batch b = new Batch();
		
		if (args.length < 2) {
			// print usage info
			System.out.println("Use with ... ");
			System.out.println("\t -Dexec.args=\"import /path/to/pdf_files/ repository_name\"");
			System.out.println("\t -Dexec.args=\"info <repo> <workingset>\"");
			System.out.println("\t -Dexec.args=\"export <repo> <workingset> <docid> <export_type>\"");
			System.out.println("\t -Dexec.args=\"exportall <repo> <workingset> <export_type>\"");
			System.out.println("\t -Dexec.args=\"convert <structure-xml> <src-pdf>\"");
			
		} else {
			String command = args[0];
			if (command.equals("import")) {
				String dir = args[1];
				String repository_name = args[2];
				b.importBatch(dir,repository_name);
			} else if (command.equals("export")) {
				String repository = args[1];
				String working_set = args[2];
				int doc_id = Integer.parseInt(args[3]);
				String export_type = args[4];
				b.exportDocument(repository, working_set, doc_id, export_type);
			} else if (command.equals("exportall")) {
				String repository = args[1];
				String working_set = args[2];
				String export_type = args[3];
				b.exportAll(repository, working_set, export_type);
			} else if (command.equals("info")) {
				String repository = args[1];
				String working_set = args[2];
				b.printRepositoryInfo(repository, working_set);
			} else if (command.equals("convert")) {
				String structure_xml = args[1];
				String pdfsrc = args[2];
				b.convertToCsv(structure_xml, pdfsrc);
			} else {
				System.out.println("Unrecognized option '" + command + "'");
			}
		}
		
		System.out.println("Done ...");
	}
}
