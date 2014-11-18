package io.crowbar.maven.plugin.diagnosis;

import io.crowbar.diagnostic.Connection;
import io.crowbar.diagnostic.Diagnostic;
import io.crowbar.diagnostic.DiagnosticReport;
import io.crowbar.diagnostic.DiagnosticSystem;
import io.crowbar.diagnostic.DiagnosticSystemFactory;
import io.crowbar.diagnostic.algorithms.FuzzinelRanker;
import io.crowbar.diagnostic.algorithms.MHSGenerator;
import io.crowbar.diagnostic.algorithms.SimilarityRanker;
import io.crowbar.diagnostic.algorithms.SingleFaultGenerator;
import io.crowbar.diagnostic.runners.JNARunner;
import io.crowbar.diagnostic.spectrum.ProbeType;
import io.crowbar.diagnostic.spectrum.Spectrum;
import io.crowbar.diagnostic.spectrum.SpectrumViewFactory;
import io.crowbar.diagnostic.spectrum.TreeView;
import io.crowbar.diagnostic.spectrum.TreeViewFactory;
import io.crowbar.diagnostic.spectrum.matchers.ActiveProbeMatcher;
import io.crowbar.diagnostic.spectrum.matchers.NegateMatcher;
import io.crowbar.diagnostic.spectrum.matchers.ProbeTypeMatcher;
import io.crowbar.diagnostic.spectrum.matchers.SuspiciousProbeMatcher;
import io.crowbar.diagnostic.spectrum.matchers.TestProbesMatcher;
import io.crowbar.diagnostic.spectrum.matchers.ValidTransactionMatcher;
import io.crowbar.diagnostic.spectrum.matchers.tree.FunctionGranularityMatcher;
import io.crowbar.diagnostic.spectrum.matchers.tree.TestNodesMatcher;
import io.crowbar.instrumentation.passes.InjectPass.Granularity;
import io.crowbar.instrumentation.spectrum.matcher.JUnitAssumeMatcher;
import io.crowbar.maven.plugin.configs.PluginConfigs;
import io.crowbar.messages.VisualizationMessages;
import io.crowbar.util.MergeStrategy;

import java.util.List;

public class DiagnosticEngine {

	private PluginConfigs configs;

	public DiagnosticEngine(PluginConfigs configs) {
		this.configs = configs;
	}

	public String diagnose(Spectrum spectrum) {

		final Spectrum spectrumView = getSpectrumView(spectrum);

		DiagnosticSystemFactory j = new DiagnosticSystemFactory();
		Connection con;

		switch(configs.getAlgorithm()) {
		case FUZZINEL:
			j.addGenerator(new MHSGenerator());
			j.addRanker(new FuzzinelRanker());            
			con = j.addConnection(0, 0);
			break;

		case SIMILARITY:
		default:
			j.addGenerator(new SingleFaultGenerator());
			j.addRanker(new SimilarityRanker(configs.getSimilarityType()));
			con = j.addConnection(0, 0);
			break;
		}
		DiagnosticSystem ds = j.create();


		try {
			JNARunner runner = new JNARunner();
			DiagnosticReport dr = runner.run(ds, spectrumView);
			Diagnostic diag = dr.getDiagnostic(con);
			List<Double> scores = spectrumView.getScorePerNode(diag, MergeStrategy.SUM);

			for(int i = 0; i < scores.size(); i++) {
				Double d = scores.get(i);
				if(Double.isNaN(d)) scores.set(i, -1d);
			}

			TreeViewFactory tvf = new TreeViewFactory(spectrumView.getTree());
			tvf.addStage(new TestNodesMatcher(spectrum));

			if (configs.getGranularity() == Granularity.FUNCTION) {
				tvf.addStage(new FunctionGranularityMatcher());
			}
			TreeView n = tvf.getView();

			scores = n.updateScores(scores);


			String jsonRequest = io.crowbar.messages.Messages.serialize(VisualizationMessages.issueRequest(n,scores));

			return jsonRequest;
		}
		catch (Throwable e) {e.printStackTrace();}

		return null;
	}

	private Spectrum getSpectrumView(Spectrum spectrum) {
		SpectrumViewFactory svf = new SpectrumViewFactory(spectrum);
		svf.addStage(new NegateMatcher(new JUnitAssumeMatcher(false)));
		svf.addStage(new NegateMatcher(new TestProbesMatcher()));
		svf.addStage(new ProbeTypeMatcher(ProbeType.HIT_PROBE));
		svf.addStage(new ActiveProbeMatcher());
		svf.addStage(new SuspiciousProbeMatcher());
		svf.addStage(new ValidTransactionMatcher()); 
		return svf.getView();
	}
}
