package io.crowbar.maven.plugin.server;

import io.crowbar.instrumentation.InstrumentationServer;
import io.crowbar.instrumentation.InstrumentationServer.Service;
import io.crowbar.instrumentation.InstrumentationServer.ServiceFactory;
import io.crowbar.instrumentation.events.EventListener;
import io.crowbar.instrumentation.spectrum.SpectrumBuilder;

import java.net.ServerSocket;

public class CrowbarServer {
	private static class CrowbarService implements Service {
		private SpectrumBuilder spectrumBuilder = new SpectrumBuilder();
		
		public EventListener getEventListener() {
			return spectrumBuilder;
		}

		public void interrupted() {
		}

		public void terminate() {
			ServerSingleton ss = ServerSingleton.instance();
			if (ss != null) {
				ss.setSpectrum(spectrumBuilder.getSpectrum());	
			}
		}
		
	}
	
	public static class CrowbarServiceFactory implements ServiceFactory {
        public final Service create (String id) {
            return new CrowbarService();
        }
    }
	
	public void start() {
    	try {
    		ServerSocket serverSocket = new ServerSocket(0);
            InstrumentationServer s = new InstrumentationServer(serverSocket,
                                  								new CrowbarServiceFactory());
            port = serverSocket.getLocalPort();
            s.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int port;
}
