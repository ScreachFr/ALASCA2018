package fr.upmc.gaspardleo.applicationvm.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface ApplicationVMConnectionsI 
		extends	OfferedI, RequiredI{
	
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception;
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception;
}
