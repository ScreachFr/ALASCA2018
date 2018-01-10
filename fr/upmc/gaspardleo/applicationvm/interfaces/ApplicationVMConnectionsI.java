package fr.upmc.gaspardleo.applicationvm.interfaces;

public interface ApplicationVMConnectionsI {
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception;
	
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception;
}
