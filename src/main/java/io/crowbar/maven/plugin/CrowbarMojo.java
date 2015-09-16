package io.crowbar.maven.plugin;

import io.crowbar.diagnostic.spectrum.Spectrum;
import io.crowbar.maven.plugin.configs.PluginConfigs;
import io.crowbar.maven.plugin.diagnosis.DiagnosticEngine;
import io.crowbar.maven.plugin.reporting.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "test")
@Execute(lifecycle = "crowbar", phase = LifecyclePhase.TEST)
public class CrowbarMojo extends AbstractCrowbarMojo {
	
	private static final String REPORT_FILENAME = "report.json";
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(shouldInstrument() && isOwnServer()) {
			getLog().info("Diagnosing...");
			
			PluginConfigs configs = new PluginConfigs(this);
			DiagnosticEngine diagnosticEngine = new DiagnosticEngine(configs);
			Spectrum spectrum = retrieveCurrentSpectrum();
			
			if(spectrum != null) {				
				String diagnosis = diagnosticEngine.diagnose(spectrum);
				//writeReport(diagnosis);
				
				try {
					ReportGenerator rp = new ReportGenerator(diagnosis);
					rp.generate(reportDirectory);
					getLog().info("Generated report: " + reportDirectory.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}

	private void writeReport(String diagnosis) {
		reportDirectory.mkdirs();
		File f = new File(reportDirectory, REPORT_FILENAME);
		try {
			f.createNewFile();
			PrintWriter writer = new PrintWriter(f);
			writer.println(diagnosis);
			writer.close();
			getLog().info("Generated report: " + f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
