package io.crowbar.maven.plugin;

import io.crowbar.diagnostic.spectrum.Spectrum;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractCrowbarMojo extends AbstractMojo {

	private static final String SPECTRUM_KEY = "CurrentSpectrum";
	
	@Parameter(property = "plugin.artifactMap")
	private Map<String, Artifact> pluginArtifactMap;
	
	@Parameter(property = "project")
	protected MavenProject project;
	
	@Parameter(defaultValue = "-1")
	protected int port;
	
	@Parameter(defaultValue = " ")
	protected String argLine;
	
	@Parameter(defaultValue = "false")
	protected boolean statementGranularity;
	
	@Parameter(defaultValue = "false")
	protected boolean fuzzinel;
	
	@Parameter(defaultValue = "false")
	protected boolean barinel;
	
	@Parameter(defaultValue = "5000")
	protected int maxCandidates;
	
	@Parameter(defaultValue = "${project.build.directory}/crowbar-report")
	protected File reportDirectory;
	
	@Parameter
	protected List<String> classesToInstrument;
	
	AbstractCrowbarMojo() {
	}
	
	public boolean shouldInstrument() {
		return project != null && !"pom".equals(project.getPackaging());
	}
	
	public boolean isOwnServer() {
		return port == -1;
	}
	
	public void setProjectProperty(String key, String value) {
		project.getProperties().setProperty(key, value);
	}
	
	public String getProjectProperty(String key) {
		return project.getProperties().getProperty(key, "");
	}
	
	public Artifact getArtifact(String name) {
		return pluginArtifactMap.get(name);
	}
	
	public void storeCurrentSpectrum(Spectrum spectrum) {
		getPluginContext().put(SPECTRUM_KEY, spectrum);
	}
	
	public Spectrum retrieveCurrentSpectrum() {
		Object obj = getPluginContext().get(SPECTRUM_KEY);
		if(obj instanceof Spectrum) {
			return (Spectrum) obj;
		}
		return null;
	}

	public int getPort() {
		return port;
	}

	public boolean isStatementGranularity() {
		return statementGranularity;
	}
	
	public boolean isFuzzinel() {
		return fuzzinel;
	}

	public boolean isBarinel() {
		return barinel;
	}

	public int getMaxCandidates() {
		return maxCandidates;
	}
}
