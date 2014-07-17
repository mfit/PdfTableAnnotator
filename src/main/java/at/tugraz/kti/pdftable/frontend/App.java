package at.tugraz.kti.pdftable.frontend;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.http.HttpRequest;

import at.tugraz.kti.pdftable.data.RepositoryAccess;

/**
 * "Well known object" to retrieve configuration settings and the object 
 * through which to access documents from.
 * 
 * @author "matthias frey"
 */
public class App {

	protected static App instance;
	protected Logger logger;
	public Properties properties;
	
	protected App() {
		
	}

	public static App getInstance() throws SecurityException, IOException {
		if (instance == null) {
			instance = new App();
			Properties prop = new Properties();
    		prop.load(new FileInputStream("config.ini"));
    		instance.properties = prop;
   			instance.setUpLogger(prop.getProperty("performance-log", ""));	
		}
		return instance;
	}
	
	public RepositoryAccess getRepositoryAccess(HttpServletRequest req) {
		RepositoryAccess ra;
		String repo 		= properties.getProperty("default-repo", "test");
		String working_set	= "default";
		if (req != null) {
			HttpSession ses = req.getSession();
			if ( ses.getAttribute("user_id") != null) {
				working_set = (String) ses.getAttribute("user_id");
			}
			if ( ses.getAttribute("repo") != null) {
				repo = (String) ses.getAttribute("repo");
			}
		}
		
		if (repo == RepositoryAccess.EXTERNAL_MODE) {
			// "external" mode
			String basedir = properties.getProperty("external-basedir", "/");
			String datadir = properties.getProperty("external-datadir", "resources/repos/external/");
			ra = RepositoryAccess.getInstance("/", working_set);
			ra.setOptions(basedir, datadir + "/documents.json", datadir);
			ra.externalMode = true;
		} else {
			// "repository" mode
			String repodir = properties.getProperty("repo-dir", "resources/repos/");
			ra = RepositoryAccess.getInstance(repodir + repo, working_set);
		}
		ra.setProperties(properties);		
		ra.setLogger(logger);
		return ra;
	}
	
	public RepositoryAccess getRepositoryAccess(String repo, String working_set) {
		String repodir = properties.getProperty("repo-dir", "resources/repos/");
		return RepositoryAccess.getInstance(repodir + repo, working_set);
	}
	
	public RepositoryAccess getRepositoryAccess() {
		String repo="test";
		String working_set="default";
		return RepositoryAccess.getInstance(repo, working_set);
	}
	
	protected void setUpLogger(String logfile) {
		logger = Logger.getLogger(RepositoryAccess.class.toString());
		
		if (logfile != "") {
			// add handler that writes to file
			Handler loghandler;
			try {
				loghandler = new FileHandler(logfile, true);
				// logh.setFormatter(new SimpleFormatter());
				logger.addHandler(loghandler);
			} catch (SecurityException e) {
				System.out.println("Could not set up performance logger");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Could not set up performance logger");
				e.printStackTrace();
			}
		}
	}
}
