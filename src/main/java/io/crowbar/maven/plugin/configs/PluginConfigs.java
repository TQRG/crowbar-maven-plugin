package io.crowbar.maven.plugin.configs;

import io.crowbar.diagnostic.algorithms.SimilarityRanker;
import io.crowbar.instrumentation.AgentConfigs;
import io.crowbar.instrumentation.passes.InjectPass.Granularity;
import io.crowbar.maven.plugin.AbstractCrowbarMojo;
import io.crowbar.maven.plugin.server.ServerSingleton;

public class PluginConfigs extends AgentConfigs {

	public static enum Algorithm { FUZZINEL, SIMILARITY }
	
	private Algorithm algorithm = Algorithm.SIMILARITY;
	private SimilarityRanker.Type similarityType = SimilarityRanker.Type.OCHIAI;
	private int maxCandidates = 5000;
	
	
	public PluginConfigs(AbstractCrowbarMojo crowbarMojo) {
		initialize(crowbarMojo);
	}

	private void initialize(AbstractCrowbarMojo crowbarMojo) {
		int port = crowbarMojo.getPort();
		if(crowbarMojo.isOwnServer()) {
			port = ServerSingleton.instance(crowbarMojo).getPort();	
		}
		
		setPort(port);
		setGranularity(crowbarMojo.isStatementGranularity() ? Granularity.STATEMENT : Granularity.FUNCTION);
		
		this.algorithm = crowbarMojo.isFuzzinel() || crowbarMojo.isBarinel() 
				? Algorithm.FUZZINEL : Algorithm.SIMILARITY;
		
		maxCandidates = crowbarMojo.getMaxCandidates();
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
	public SimilarityRanker.Type getSimilarityType() {
		return similarityType;
	}
	
	public int getMaxCandidates() {
		return maxCandidates;
	}
}
