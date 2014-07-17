package at.tugraz.kti.pdftable.frontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import at.tugraz.kti.pdftable.data.RepositoryAccess;
import at.tugraz.kti.pdftable.data.RepositoryException;
import at.tugraz.kti.pdftable.document.TableCell;
import at.tugraz.kti.pdftable.document.DocumentTable;
import at.tugraz.kti.pdftable.document.DocumentException;
import at.tugraz.kti.pdftable.document.RichDocument;
import at.tugraz.kti.pdftable.document.TableModel;
import at.tugraz.kti.pdftable.document.DocumentTables;
import at.tugraz.kti.pdftable.document.export.DOMExport;
import at.tugraz.kti.pdftable.document.export.Export;
import at.tugraz.kti.pdftable.document.export.ExportException;
import at.tugraz.kti.pdftable.document.export.HtmlExport;
import at.tugraz.kti.pdftable.document.export.LogicalStructureExport;
import at.tugraz.kti.pdftable.document.export.RegionExport;
import at.tugraz.kti.pdftable.document.export.StructureExport;
import at.tugraz.kti.pdftable.extract.TableDetectionFromRegion;

/**
 * 
 * @author "matthias frey"
 * 
 *         Defines REST resources endpoints.
 * 
 */
@Path("/")
@Produces("text/plain")
public class Resource {

	App app;

	public Resource() throws SecurityException, IOException {
		app = App.getInstance();
	}

	@GET
	@Path("/settings")
	public Response getSettings(@Context HttpServletRequest req)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		HttpSession ses = req.getSession();
		HashMap<String, String> output = new HashMap<String, String>();
		output.put("user_id", (String) ses.getAttribute("user_id"));
		output.put("repo", app.getRepositoryAccess(req).getRepositoryName());
		return Response.ok(mapper.writeValueAsString(output),
				MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/settings")
	public Response setSettings(@Context HttpServletRequest req)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		HttpSession ses = req.getSession();
		if (req.getParameterMap().containsKey("user_id")) {
			ses.setAttribute("user_id", req.getParameter("user_id"));
		}
		if (req.getParameterMap().containsKey("repo")) {
			if ( req.getParameter("repo") != RepositoryAccess.EXTERNAL_MODE) {
				ses.setAttribute("repo", req.getParameter("repo"));
			}
		}
		HashMap<String, String> output = new HashMap<String, String>();
		output.put("user_id", (String) ses.getAttribute("user_id"));
		output.put("repo", app.getRepositoryAccess(req).getRepositoryName());
		return Response.ok(mapper.writeValueAsString(output),
				MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a list of the available documents.
	 * 
	 * @return
	 * @throws RepositoryException
	 */
	@GET
	@Path("/documents")
	public Response getDocuments(@Context HttpServletRequest req)
			throws RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		ObjectMapper mapper = new ObjectMapper();
		String str = "";
		try {
			HashMap<Integer, String> result = new HashMap<Integer, String>();
			Iterator entries = repo.getDocuments().entrySet().iterator();
			while (entries.hasNext()) {
				Entry<Integer, String> thisEntry = 
						(Entry<Integer, String>) entries.next();
				Integer key = thisEntry.getKey();
				File f = new File(thisEntry.getValue());
				result.put(key, f.getName());
			}
			str = mapper.writeValueAsString(result);
			return Response.ok(str, MediaType.APPLICATION_JSON).build();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.status(404).build();

	}

	/**
	 * return the original pdf
	 * 
	 * @param docid
	 * @return
	 * @throws IOException
	 * @throws RepositoryException
	 */
	@GET
	@Path("/document/{docid}/pdf")
	public Response getSourceFile(@PathParam("docid") Integer docid,
			@Context HttpServletRequest req) throws IOException,
			RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		String fileName;
		try {
			fileName = repo.getSourceDirectory() + "/"
					+ repo.getDocuments().get(docid);

		} catch (NullPointerException e) {
			return Response.status(404).build();
		}

		// log action
		repo.getLogger().info(
				new LogAction(repo.getWorkingSet(), 
						repo.getDocumentById(docid).getFilename(),
						docid, 0, 0, "source.get", "").toString());

		File f = new File(fileName);
		ResponseBuilder response = Response.ok((Object) f);
		response.type("application/pdf");
		response.header("Content-Disposition", "attachment; filename=\""
				+ repo.getDocuments().get(docid) + "\"");
		return response.build();
	}

	/**
	 * Retrieve document meta data.
	 * For a document (id), return the filename, the docid and 
	 */
	@GET
	@Path("/document/{docid}/meta")
	public Response getDocumentMetaData(@PathParam("docid") Integer docid,
			@Context HttpServletRequest req) throws JsonParseException,
			JsonMappingException, IOException, RepositoryException {

		RichDocument doc;

		RepositoryAccess repo = app.getRepositoryAccess(req);

		try {
			doc = repo.getDocumentById(docid);
		} catch (NullPointerException e) {
			return Response.status(404).build();
		}

		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> result_data = new HashMap<String, String>();
		HashMap<String, Integer> tables_dir = new HashMap<String, Integer>();

		result_data.put("filename", doc.getFilename());
		result_data.put("docid", String.valueOf(docid));
		result_data.put("pages", String.valueOf(doc.getNumberOfPages()));

		repo.getLogger()
				.info(new LogAction(repo.getWorkingSet(), 
						repo.getDocumentById(docid).getFilename(),
						docid, 0, 0, "meta.get", "").toString());

		// add occurances of table annotations - 
		// TODO : need to encode properly (?) to really transmit table data
		for (DocumentTable t : doc.getTableAnnotations().getTables()) {
			tables_dir.put(String.valueOf(t.tn), t.page);
		}
		// result_data.put("tables", mapper.writeValueAsString(tables_dir));

		result_data.put("tables_count", String.valueOf(tables_dir.size()));
		return Response.ok(mapper.writeValueAsString(result_data),
				MediaType.APPLICATION_JSON).build();

	}

	/**
	 * add a table
	 * 
	 * @param docid
	 * @param pageid
	 * @param req
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@POST
	@Path("/document/{docid}/page/{pageid}/table")
	public Response saveAnnotationTable(@PathParam("docid") Integer docid,
			@PathParam("pageid") Integer pageid, @Context HttpServletRequest req)
			throws JsonParseException, JsonMappingException, IOException,
			RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		String json = "";
		try {
			BufferedReader reader;
			reader = req.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				json = json + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().entity("read error").build();
		}

		System.out.println(json);

		// unwrap the provided json-string
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		TypeReference deserializedType = new TypeReference<TableModel>() {
		};
		TableModel incomingTable = null;
		incomingTable = mapper.readValue(json, deserializedType);

		System.out.println(incomingTable);

		// store in repository
		repo.addTableToDocument(docid, pageid, incomingTable);

		// log
		repo.getLogger().info(
				new LogAction(repo.getWorkingSet(), 
						repo.getDocumentById(docid).getFilename(),
						docid, pageid, 0, "table.save", "").toString());

		return Response.ok().build();
	}

	/**
	 * Get (annotated, topological) table info for all tables on a page of a
	 * document.
	 * 
	 * @param docid
	 * @param pageid
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("/document/{docid}/page/{pageid}/tables")
	// @Produces("application/json")
	public String getAnnotationTables(@PathParam("docid") Integer docid,
			@PathParam("pageid") Integer pageid, @Context HttpServletRequest req)
			throws JsonParseException, JsonMappingException, IOException,
			RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		ObjectMapper mapper = new ObjectMapper();
		ArrayList<DocumentTable> result = repo.getAnnotatedTables(docid, pageid);
		return mapper.writeValueAsString(result);
	}

	/**
	 * delete one specific table
	 * 
	 * @param docid
	 * @param pageid
	 * @param tableid
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@DELETE
	@Path("/document/{docid}/page/{pageid}/table/{tableid}")
	public Response clearAnnotationTables(@PathParam("docid") Integer docid,
			@PathParam("pageid") Integer pageid,
			@PathParam("tableid") Integer tableid,
			@Context HttpServletRequest req) throws JsonParseException,
			JsonMappingException, IOException, RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		repo.clearTable(docid, pageid, tableid);

		// log action
		repo.getLogger().info(
				new LogAction(repo.getWorkingSet(), repo.getDocumentById(docid)
						.getFilename(), docid, pageid, tableid, "table.delete",
						"").toString());

		return Response.ok().build();
	}

	/**
	 * experimental
	 * 
	 * @param docid
	 * @param pageid
	 * @param region
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws DocumentException
	 */
	@GET
	@Path("/document/{docid}/page/{pageid}/classifier/table_from_region")
	public String callClassifier(@PathParam("docid") Integer docid,
			@PathParam("pageid") Integer pageid,
			@QueryParam("region") String region, @Context HttpServletRequest req)
			throws JsonParseException, JsonMappingException, IOException,
			DocumentException, RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		RichDocument doc = repo.getDocumentById(docid);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		TypeReference deserializedType = new TypeReference<ArrayList<Float>>() {
		};
		ArrayList<Float> rect = null;
		rect = mapper.readValue(region, deserializedType);

		TableDetectionFromRegion tdet = new TableDetectionFromRegion();
		DocumentTable detected = tdet.extract(doc, pageid, rect);

		return mapper.writeValueAsString(detected);
	}

	/**
	 * return table export file
	 * 
	 * @param docid
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@GET
	@Path("/document/{docid}/export/{export_name}")
	public Response getTableExport(@PathParam("docid") Integer docid,
			@PathParam("export_name") String export_name,
			@QueryParam("disposition") String disp,
			@Context HttpServletRequest req) throws JsonParseException,
			JsonMappingException, IOException, RepositoryException,
			ExportException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		// load doc or 404
		RichDocument doc = null;

		try {
			doc = repo.getDocumentById(docid);
		} catch (IOException e) {
			return Response.status(404).build();
		}

		// instantiate export or 404
		Export exp = Export.factory(export_name);
		// System.out.println("Request for export " + export_name);

		if (exp == null) {
			return Response.status(404).build();
		}

		// export to string
		exp.setDocument(doc);
		exp.export();
		String output = exp.toString();

		// serve inline or as attachment
		if (disp != null && disp.toLowerCase().equals("attachment")) {
			ResponseBuilder respbuilder = Response.ok(output);
			respbuilder.header("Content-Disposition", "attachment; filename=\""
					+ exp.getExportFilename(doc.getFilename()) + "\"");
			return respbuilder.build();
		} else {
			Response result = Response.ok(output, exp.getMimeType()).build();
			return result;
		}

	}

	// /**
	// * retrieve data about automatically extracted tables
	// */
	// @GET
	// @Path("/document/{docid}/extract-tables")
	// public Response getAutoExtractedTables(@PathParam("docid") Integer docid)
	// throws JsonParseException, JsonMappingException, IOException {
	//
	// // String reponame = "test";
	// // String workingset = "default";
	// // RepositoryAccess repo = RepositoryAccess.getInstance(reponame,
	// // workingset);
	//
	// Response result = Response.ok(dp.getAutoExtractedTables(docid),
	// MediaType.TEXT_HTML).build();
	// return result;
	// }

	/**
	 * clear all tables of one page in a document
	 * 
	 * @param docid
	 * @param pageid
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@DELETE
	@Path("/document/{docid}/tables")
	public Response clearAnnotationTables(@PathParam("docid") Integer docid,
			@Context HttpServletRequest req) throws IOException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		repo.resetAnnotations(docid);
		return Response.ok().build();
	}

	@GET
	@Path("/document/{docid}/write")
	public Response writeExports(@PathParam("docid") Integer docid,
			@Context HttpServletRequest req) throws RepositoryException,
			IOException, ExportException {
		RepositoryAccess repo = app.getRepositoryAccess(req);

		RichDocument doc;
		doc = repo.getDocumentById(docid);

		repo.writeExports(doc);
		return Response.ok().build();
	}

	/**
	 * load a page-data-layer by document and page
	 * 
	 * @param docid
	 * @param pageid
	 * @param layerid
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	@GET
	@Path("/document/{docid}/page/{pageid}/layer/{layerid}")
	public Response getDocPageLayer(@PathParam("docid") Integer docid,
			@PathParam("pageid") Integer pageid,
			@PathParam("layerid") String layerid,
			@Context HttpServletRequest req) throws IOException,
			RepositoryException {

		RepositoryAccess repo = app.getRepositoryAccess(req);

		repo.getLogger().info(
				new LogAction(repo.getWorkingSet(), repo.getDocumentById(docid)
						.getFilename(), docid, pageid, 0, "layer.get."
						+ layerid, "").toString());

		return Response.ok(repo.getDataLayerAsJson(docid, pageid, layerid),
				MediaType.APPLICATION_JSON).build();
	}

}
