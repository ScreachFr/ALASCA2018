package fr.upmc.gaspardleo.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatcherOutboundPort 
	extends AbstractOutboundPort
	implements RequestDispatcherI{

	public RequestDispatcherOutboundPort(ComponentI owner) throws Exception {
		super(RequestDispatcherI.class, owner);
	}

	@Override
	public String registerVM(String vmUri, String requestSubmissionOutboundPort, Class<?> vmInterface)
			throws Exception {
		return ((RequestDispatcherI)this.connector).registerVM(vmUri, requestSubmissionOutboundPort, vmInterface);
	}

	@Override
	public void unregisterVM(String vmUri) throws Exception {
		((RequestDispatcherI)this.connector).unregisterVM(vmUri);
	}
}
