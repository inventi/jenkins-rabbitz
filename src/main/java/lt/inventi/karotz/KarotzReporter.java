package lt.inventi.karotz;

import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepDescriptor;

public interface KarotzReporter {
	
	public String prebuild(AbstractBuild<?, ?> build, BuildStepDescriptor<?> descriptor);
	
	public String perform(AbstractBuild<?, ?> build, BuildStepDescriptor<?> descriptor); 

}
