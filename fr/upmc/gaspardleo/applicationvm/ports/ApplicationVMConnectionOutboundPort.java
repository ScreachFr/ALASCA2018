package fr.upmc.gaspardleo.applicationvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;

public class ApplicationVMConnectionOutboundPort 
	extends AbstractOutboundPort implements ApplicationVMConnectionsI {

	private static final long serialVersionUID = 1L;

	public ApplicationVMConnectionOutboundPort(String uri, ComponentI owner)
			throws Exception {
		super(uri, ApplicationVMConnectionsI.class, owner);
	}

	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {
		((ApplicationVMConnectionsI)this.connector).doRequestNotificationConnection(RD_RequestNotificationInboundPortURI);
	}

	@Override
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception {
		((ApplicationVMConnectionsI)this.connector).doRequestMonitorConnection(requestMonitor_in);
	}

}
