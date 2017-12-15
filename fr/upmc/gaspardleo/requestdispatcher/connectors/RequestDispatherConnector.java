package fr.upmc.gaspardleo.requestdispatcher.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatherConnector 
	extends AbstractConnector
	implements RequestDispatcherI{

	@Override
	public String registerVM(String vmUri, String requestSubmissionOutboundPort, Class<?> vmInterface) throws Exception {
		return ((RequestDispatcherI)this.offering).registerVM(vmUri, requestSubmissionOutboundPort, vmInterface);		
	}

	@Override
	public void unregisterVM(String vmUri) throws Exception {
		((RequestDispatcherI)this.offering).unregisterVM(vmUri);
	}
}
