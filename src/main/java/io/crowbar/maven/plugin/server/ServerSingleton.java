package io.crowbar.maven.plugin.server;

import io.crowbar.diagnostic.spectrum.Spectrum;
import io.crowbar.maven.plugin.AbstractCrowbarMojo;

public class ServerSingleton {

	private static ServerSingleton instance = null;
	
	public static ServerSingleton instance(AbstractCrowbarMojo delegate) {
		if(instance == null) {
			instance = new ServerSingleton();
		}
		
		if(delegate != null) {
			instance.setDelegate(delegate);
		}
		return instance;
	}
	
	public static ServerSingleton instance() {
		return instance(null);
	}
	
	private CrowbarServer crowbarServer;
	private AbstractCrowbarMojo delegate;
	
	private ServerSingleton() {
		crowbarServer = new CrowbarServer();
		crowbarServer.start();
	}
	
	private void setDelegate(AbstractCrowbarMojo delegate) {
		this.delegate = delegate;
	}
	
	public int getPort() {
		return crowbarServer.port;
	}
	
	public void setSpectrum(Spectrum spectrum) {
		delegate.storeCurrentSpectrum(spectrum);
	}
	
}
