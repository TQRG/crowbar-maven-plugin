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
import io.crowbar.diagnostic.spectrum.Node;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiagnosticEngine {

	private PluginConfigs configs;

	public DiagnosticEngine(PluginConfigs configs) {
		this.configs = configs;
	}

	public String[] diagnose(Spectrum spectrum) {

		final Spectrum spectrumView = getSpectrumView(spectrum);

		DiagnosticSystemFactory j = new DiagnosticSystemFactory();
		Connection con;

		switch(configs.getAlgorithm()) {
		case FUZZINEL:
			MHSGenerator generator = new MHSGenerator();
			generator.setMaxCandidates(configs.getMaxCandidates());
			
			j.addGenerator(generator);
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
			List<int[]> freqs = spectrumView.getFreqsPerNode();
			
			TreeView n = tvf.getView();
			scores = n.updateScores(scores);
			freqs = n.updateFreqs(freqs);
			
			StringBuilder report = new StringBuilder();
			List<Node> treeNodes = n.getNodes();
			
			List<NodeScore> nodeScores = new ArrayList<NodeScore>();
			for (int i = 0; i < scores.size(); i++) {
				Node node = treeNodes.get(i);
				double score = scores.get(i);
				
				if (node.getChildren() == null || node.getChildren().isEmpty()) { //terminal node
					nodeScores.add(new NodeScore(node, score));
				}
			}
			
			Collections.sort(nodeScores, new Comparator<NodeScore>() {
				public int compare(NodeScore o1, NodeScore o2) {
					if (o1.score == o2.score)
						return 0;
					else if (o1.score < o2.score)
						return 1;
					else
						return -1;
				}
			});
			
			
			for(NodeScore ns : nodeScores) {
				report.append(ns);
				report.append('\n');
			}
			String reportString = report.toString();
			
			String jsonRequest = io.crowbar.messages.Messages.serialize(VisualizationMessages.issueRequest(n,scores,freqs));

			return new String[] {jsonRequest, reportString};
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
	
	private class NodeScore {
		public final Node node;
		public final double score;
		
		public NodeScore(Node node, double score) {
			if (score < 0d) {
				score = 0d;
			}
			this.score = score;
			this.node = node;
		}
		
		public String toString() {
			return node.getFullName(".", 1) + "\t" + score;
		}
	}
}
