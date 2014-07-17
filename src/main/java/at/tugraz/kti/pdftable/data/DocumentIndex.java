package at.tugraz.kti.pdftable.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Manage an index of documents, that stores documents as relative paths. 
 * 
 * @author "matthias frey"
 *
 */
public class DocumentIndex {

	protected File index, repo_base;
	protected HashMap<Integer, String> documents;

	public DocumentIndex(String indexfile) {
		index		= new File(indexfile);
		repo_base 	= null;
		documents = null;
	}
	
	public DocumentIndex(String indexfile, String repo_base) {
		index 			= new File(indexfile);
		this.repo_base 	= new File(repo_base);
		documents = null;
	}

	public void init() throws RepositoryException {
		if (!index.exists()) {
			documents = new HashMap<Integer, String>();
			save();
		}
	}
	
	public File getBasePath() {
		if (repo_base!=null) return repo_base;
		else return new File("");
	}

	public void open() throws RepositoryException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference deserializedType = new TypeReference<HashMap<Integer, String>>() {
		};
		init();
		try {
			documents = mapper.readValue(index, deserializedType);
		} catch (JsonParseException e) {
			throw new RepositoryException(e);
		} catch (JsonMappingException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}
	
	public void save() throws RepositoryException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference deserializedType = new TypeReference<HashMap<Integer, String>>() {
		};
		try {
			mapper.writeValue(index, documents);
		} catch (JsonGenerationException e) {
			throw new RepositoryException(e);
		} catch (JsonMappingException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}	
	}

	public File getDocumentById(int id) throws RepositoryException {
		open();
		if (documents.containsKey(id)) {
			if ( repo_base != null ) {
				return new File(repo_base, documents.get(id));	
			} else {
				return new File(documents.get(id));
			}
		}
		return null;
	}

	public HashMap<Integer, String> getDocuments() throws RepositoryException {
		open();
		return documents;
	}

	public int add(File document) throws RepositoryException {
		open();
		int maxid = 0;
		int newid = 0;
		for (int i : documents.keySet()) {
			if (i > maxid) {
				maxid = i;
			}
			if (documents.get(i).equals(document.toString())) {
				throw new RepositoryException("Duplicate File");
			}
		}
		newid = maxid + 1;
		documents.put(newid, document.toString());
		save();
		return newid;
	}

	public int getIdByDocumentName(File document) throws RepositoryException {
		open();
		for (int i : documents.keySet()) {
			if (documents.get(i).equals(document.toString())) {
				return i;
			}
		}
		return -1;
	}
	
}
