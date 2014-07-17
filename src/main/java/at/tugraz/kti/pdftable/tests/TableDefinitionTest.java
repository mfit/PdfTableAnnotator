package at.tugraz.kti.pdftable.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentTables;

public class TableDefinitionTest {

	@Test
	public void test() throws JsonGenerationException, JsonMappingException, IOException {
		
		DocumentTables tables;
		tables = new DocumentTables();
		
		DocumentTable table = new DocumentTable();
		
		table.trs.add(new ArrayList<TableCell>());
		table.trs.add(new ArrayList<TableCell>());

		table.trs.get(0).add(new TableCell(40,40,100,50));
		table.trs.get(0).add(new TableCell(120,40,200,50));
		
		table.trs.get(1).add(new TableCell(40,60,100,70));
		table.trs.get(1).add(new TableCell(120,60,200,70));
		
		tables.addTable(2, table);
		
		ObjectMapper mp = new ObjectMapper();
		String str = mp.writeValueAsString(tables);
		// Compare textual representation of the beginnig of the json encoded datastructure: 
		assertEquals("{\"annotationsOnPage\":{\"2\":[{\"page\":2,\"tn\":1,\"trs\":[[{\"visgrid\":[40.0,40.0,100.0,50.0]",
				str.substring(0,85));
		
		
		DocumentTable table2 = new DocumentTable();
		table2.trs.add(new ArrayList<TableCell>());
		table2.trs.get(0).add(new TableCell(40,40,100,50));
		tables.addTable(3, table2);
		
		assertEquals(0, tables.getTables(0).size());
		assertEquals(1, tables.getTables(2).size());
		assertEquals(1, tables.getTables(3).size());
		assertEquals(2, tables.getTables().size());
		
	}

}
