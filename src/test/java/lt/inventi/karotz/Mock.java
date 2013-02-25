package lt.inventi.karotz;

import hudson.model.Result;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mock {
	
	public static class Jenkins {
		
		public String getRootUrl(){
			return "aaa";
		}
	}
	
	public static class Descriptor {
		
		public String getApiKey() {
			return "API-KEY";
		}

		public String getSecretKey() {
			return "SECRET-KEY";
		}

		public Map<String, String> getInteractiveIds() {
			Map<String, String> map = new HashMap<String, String>();
			map.put("INSTALLATION", "INTERACTIVE-ID");
			return map;
		}
	}
	
	public static class Build{
		public Build getProject(){
			return this;
		}
		
		public String getName(){
			return "BUILD-NAME";
		}
		
		public Result getResult(){
			return Result.FAILURE;
		}
		
		public List<Build> getChangeSet(){
			List<Build> list =  new ArrayList<Build>();
			list.add(this);
			return list;
		}
		
		public Build getAuthor(){
			return this;
		}
		
		public String getId(){
			return "AUTHOR";
		}
		
		public Build getWorkspace(){
			return this;
		}
		
		public URI toURI(){
			return new File("./target").toURI();
		}
		
		public URL getUrl(){
			try {
				return new File("./target").toURI().toURL();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
