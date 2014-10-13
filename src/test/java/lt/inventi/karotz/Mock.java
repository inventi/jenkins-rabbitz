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
			return "http://";
		}
	}
	
	public static class Descriptor {
		
		public String getApiKey() {
			return "API-KEY";
		}

		public String getSecretKey() {
			return "SECRET-KEY";
		}
		
		public List<String> getInstallations() {
			List<String> list= new ArrayList<String>();
			list.add("INSTALLATION1");
			list.add("INSTALLATION2");
			list.add("INSTALLATION3");
			return list;
		}

		public List<String> getTokenIds() {
			List<String> list= new ArrayList<String>();
			list.add("INTERACTIVE-ID1");
			list.add("INTERACTIVE-ID2");
			return list;
		}
	}
	
	public static class EmptyDescriptor extends Descriptor{
		
		public List<String> getTokenIds() {
			return null;
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
		
		public String getUrl(){
			return "project";
		}
		
		public Build getPreviousBuild(){
			return this;
		}
	}

}
