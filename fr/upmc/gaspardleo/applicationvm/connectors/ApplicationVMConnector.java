package fr.upmc.gaspardleo.applicationvm.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;

public class ApplicationVMConnector 
	extends AbstractConnector
	implements ApplicationVMConnectionsI{

	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {
		((ApplicationVMConnectionsI)this.offering).doRequestNotificationConnection(RD_RequestNotificationInboundPortURI);
	}
}
