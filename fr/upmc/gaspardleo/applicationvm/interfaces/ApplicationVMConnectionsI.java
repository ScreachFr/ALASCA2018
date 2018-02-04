package fr.upmc.gaspardleo.applicationvm.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L'interface <code> ApplicationVMConnectionsI </ code> définit le comportement de l'objet ApplicationVM
 * pour la connexion à d'autre composant.
 * 
 * @author Leonor & Alexandre
 */
public 	interface 	ApplicationVMConnectionsI 
		extends		OfferedI, 
					RequiredI {
	
	/** 
	 * Connecte l'application VM au RequestDispatcher pour l'envoie de notifications.
	 * @param 	RD_RequestNotificationInboundPortURI  URI du port entrant du RequestDispatcher pour les notifications.
	 * @throws 	Exception
	 */
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception;
	
	/**
	 * Connecte l'application VM au RequestMonitor.
	 * @param 	requestMonitor_in		URI du port entrant du RequestMonitor.
	 * @throws 	Exception
	 */
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception;
}
