package at.tugraz.kti.pdftable.document.export;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.eclipse.jetty.util.StringUtil;
import org.w3c.dom.*;

import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.extract.BlockInfo;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Creates tables-export of a document in HTML format
 */
public class CsvExport extends Export {
	public CsvExport() {
		super();
		format = "csv";
	}
	
	protected ArrayList<String[]> toArray() {
		ArrayList<String[]> grid = new ArrayList<String[]>();
		for (DocumentTable t : doc.getTableAnnotations().getTables()) {
			int rowi = 0, celli = 0;
			
			for(HashMap<Integer, TableCell> row : t.getAsGrid().values()) {
				celli = 0;
				ArrayList<String> l = new ArrayList<String>();
				for (TableCell cell : row.values()) {
					try {
						if(cell.startrow==rowi && cell.startcol==celli) {
							String cell_contents = new BlockInfo(
									doc.getCharactersInBoundingBox(t.page, cell)).
									getCharactersAsString().trim();
							
							// System.out.println(cell_contents);
							
							l.add(cell_contents);
						} else {
							// Spanning cell - use empty contents for 
							// grid positions other than the first. 
							l.add("");
						}
						
					} catch(DocumentException e) {
						l.add(e.getMessage());
					}
					celli++;
				}
				String[] l0 = new String[l.size()];
				l0 = l.toArray(l0);
				grid.add(l0);
				rowi++;
			}
			grid.add(new String[0]);
			grid.add(new String[0]);
			grid.add(new String[0]);
		}
		return grid;
	}
	
	public String toString() {
		StringWriter output = new StringWriter();
		CSVWriter writer = new CSVWriter(output, ',');
		try {
			writer.writeAll(toArray());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toString();
	}
	
	/**
	 * dom transformed to file
	 * @param outfile
	 * @throws IOException 
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public void toFile(File outfile) throws  ExportException, IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(outfile), ',');
		writer.writeAll(toArray());
		writer.close();
	}
}