package lt.inventi.karotz;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class KarotzNotifier extends Notifier {

	private transient KarotzReporter reporter;
	
	private static final Logger log = Logger.getLogger(KarotzNotifier.class.getName());

	@DataBoundConstructor
	public KarotzNotifier() {
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		KarotzDescriptor descriptor = (KarotzDescriptor)getDescriptor();
		descriptor.tokenIds = reporter().prebuild(build, descriptor);
		log.log(Level.INFO, "prebuild: saving descriptor "+descriptor.toString());
		descriptor.save();
		return true;
	}
	
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		KarotzDescriptor descriptor = (KarotzDescriptor)getDescriptor();
		descriptor.tokenIds = reporter().perform(build, descriptor);
		log.log(Level.INFO, "preform: saving descriptor "+descriptor.toString());
		descriptor.save();
		return true;
	}
	
	private KarotzReporter reporter() {
		if (reporter == null) {
			try {
				Thread.currentThread().setContextClassLoader(
						KarotzNotifier.class.getClassLoader());

				Class<KarotzReporter> notifier = (Class<KarotzReporter>) KarotzNotifier.class
						.forName("lt.inventi.karotz.KarotzClojureReporter");

				reporter = notifier.newInstance();

			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return reporter;
	}

	@Extension
	public static class KarotzDescriptor extends
			BuildStepDescriptor<Publisher> {

		private String apiKey;

		private List<String> installations;

		private String secretKey;

		private List<String> tokenIds;

		public KarotzDescriptor() {
			load();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project
			// types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Notify karotz";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			apiKey = formData.getString("apiKey");
			installations = new ArrayList<String>();
			if(formData.get("installId") instanceof JSONArray){
				JSONArray installs = formData.getJSONArray("installId");
				for(Object obj : installs){
					installations.add(((JSONObject)obj).getString("id"));
				}
			}else{
				installations.add(formData.getJSONObject("installId").getString("id"));
			}
			secretKey = formData.getString("secretKey");
			save();

			return true;
		}

		public String getApiKey() {
			return apiKey;
		}

		public List<String> getInstallations() {
			return installations;
		}

		public String getSecretKey() {
			return secretKey;
		}
		
		public List<String> getTokenIds() {
			if(tokenIds == null){
				tokenIds = new ArrayList<String>();				
			}			
			return tokenIds;
		}
		
		@Override
		public String toString() {
			String result = "Tokens: ";
			for(String token : tokenIds){
				result += " " + token; 
			}
			return result;
		}
	}
}
