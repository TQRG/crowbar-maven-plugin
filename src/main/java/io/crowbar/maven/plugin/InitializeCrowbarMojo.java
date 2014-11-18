package io.crowbar.maven.plugin;

import io.crowbar.maven.plugin.configs.PluginConfigs;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "init")
public class InitializeCrowbarMojo extends AbstractCrowbarMojo {

	static final String SUREFIRE_ARG_LINE = "argLine";
	static final String CROWBAR_ARTIFACT = "io.crowbar:instrumentation-java";

	public void execute() throws MojoExecutionException, MojoFailureException {

		if (shouldInstrument()) {
			getLog().info("Setting up instrumentation agent");
			
			String agentFilename = getArtifact(CROWBAR_ARTIFACT).getFile().getAbsolutePath();
			PluginConfigs agentConfigs = new PluginConfigs(this);
			setAgent(agentFilename, agentConfigs);
		}
	}
	
	private void setAgent(String filename, PluginConfigs configs) {
		StringBuilder sb = new StringBuilder();

		sb.append(getProjectProperty(SUREFIRE_ARG_LINE))
			.append(" ")
			.append(argLine)
			.append(" -javaagent:\"")
			.append(filename)
			.append("\"=")
			.append(configs.serialize());
		
		setProjectProperty(SUREFIRE_ARG_LINE, sb.toString());
	}
}
