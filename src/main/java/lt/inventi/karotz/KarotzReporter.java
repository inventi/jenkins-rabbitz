package lt.inventi.karotz;

import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepDescriptor;

import java.util.Map;

public interface KarotzReporter {
	
	public Map<String, String> prebuild(AbstractBuild<?, ?> build, BuildStepDescriptor<?> descriptor);
	
	public Map<String, String> perform(AbstractBuild<?, ?> build, BuildStepDescriptor<?> descriptor); 

}
