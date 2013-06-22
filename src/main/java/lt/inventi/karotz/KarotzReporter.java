package lt.inventi.karotz;

import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepDescriptor;

import java.util.List;

public interface KarotzReporter {
	
	public List<String> prebuild(AbstractBuild<?, ?> build, BuildStepDescriptor<?> descriptor);
	
	public List<String> perform(AbstractBuild<?, ?> build, BuildStepDescriptor<?> descriptor); 

}
