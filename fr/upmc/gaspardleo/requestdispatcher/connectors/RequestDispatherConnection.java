package fr.upmc.gaspardleo.requestdispatcher.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatherConnection 
	extends AbstractConnector
	implements RequestDispatcherI{

	@Override
	public String registerVM(String vmUri, String requestSubmissionOutboundPort) throws Exception {
		return ((RequestDispatcherI)this.offering).registerVM(vmUri, requestSubmissionOutboundPort);		
	}

	@Override
	public void unregisterVM(String vmUri) throws Exception {
		((RequestDispatcherI)this.offering).unregisterVM(vmUri);
	}
}
