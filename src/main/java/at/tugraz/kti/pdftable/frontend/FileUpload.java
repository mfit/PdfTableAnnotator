package at.tugraz.kti.pdftable.frontend;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tugraz.kti.pdftable.data.RepositoryAccess;

/**
 * Servlet to manage file uploads. 
 * 
 * Modified from http://docs.codehaus.org/display/JETTY/File+Upload+in+jetty6
 * 
 */
@SuppressWarnings("serial")
public class FileUpload extends HttpServlet {
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		StringBuffer buff = new StringBuffer();

		File file1 = (File) request.getAttribute("uploadfile");

		if (file1 == null || !file1.exists()) {
			// buff.append( "File does not exist" );
		} else if (file1.isDirectory()) {
			buff.append("File is a directory");
		} else {
			File outputFile = new File("resources/incoming/"
					+ request.getParameter("uploadfile"));
			file1.renameTo(outputFile);
			buff.append("Upload OK :" + outputFile.getName());
			
			RepositoryAccess repo = App.getInstance().
					getRepositoryAccess(request);
			
			try {
				repo.addDocument(outputFile);
				buff.append(" - Import OK");
			} catch (Exception e) {
				buff.append(" - Error : Could not import file (maybe invalid type, or file with same name already exists)"/* + e.getMessage()*/);
			}
		}

		response.getWriter().write("<html>");
		response.getWriter()
				.write("<head>"
						+ "<title>Filesystem</title>"
						+ "<link type=\"text/css\" rel=\"stylesheet\" href=\"css/site.css\">"
						+ "</head>");
		response.getWriter().write("<body>");
		response.getWriter()
				.write("<form action=\"repo\" method=\"post\" enctype=\"multipart/form-data\">"
						+ "<input type=\"file\" name=\"uploadfile\"/>"
						+ "<input type=\"submit\" value=\"Send\"/>" + "</form>");
		response.getWriter().write("<p>" + buff.toString() + "</p>");
		response.getWriter().write("</body>");
		response.getWriter().write("</html>");
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}