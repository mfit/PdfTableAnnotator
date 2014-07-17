package at.tugraz.kti.pdftable.frontend;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import at.tugraz.kti.pdftable.data.RepositoryAccess;
import at.tugraz.kti.pdftable.data.RepositoryException;
import at.tugraz.kti.pdftable.document.DocumentException;

/**
 * Servlet to import a single file into the 'external' repository.
 * 
 * File is imported (if non-existent) and then redirect is made so the user can 
 * work on the file in the usual manner.
 *   
 * @author "matthias frey"
 *
 */
public class Annotate extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    			throws ServletException, IOException
    {
    	int id;
    	File doc;
    	
    	String filename = request.getParameter("file");
    	if (filename != null) {
    		
    		// set session to use repository in "external" mode
    		HttpSession ses = request.getSession();
    		ses.setAttribute("repo", RepositoryAccess.EXTERNAL_MODE);
    		ses.setAttribute("user_id", "default");
    		
    		RepositoryAccess ra = App.getInstance().getRepositoryAccess(request);
    		
    		doc = new File(ra.getSourceDirectory(), filename);
    		
    		if (! doc.isFile()) {
    			throw new IOException("File " + doc.getAbsolutePath() + " does " +
    					"not exist");
    		}
    		
    		try {
    			id = ra.getDocumentIndex().getIdByDocumentName(doc);
        		if ( id == -1) {
        			// Import as new doc.
        			id = ra.indexDocument(doc);
        		} 
        	} catch (RepositoryException e) {
				throw new ServletException(e);
			}
    		
    		// Redirect to index.html, setting document id /page (e.g. 1/0 )
    		response.sendRedirect("/?" + id);
    	}
    	
    	response.getWriter().println("To open a document, set the parameter \"file\"" +
    			" to be a path (e.g. ?file=mydoc.pdf) !" +
    			"\nNote: the path muste be relative to what is configured to be the basepath (see config.ini)");
    	response.setStatus(HttpServletResponse.SC_OK);
        
    }
}
