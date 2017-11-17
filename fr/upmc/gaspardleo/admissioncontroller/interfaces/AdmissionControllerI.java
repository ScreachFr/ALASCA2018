package fr.upmc.gaspardleo.admissioncontroller.interfaces;

import java.util.ArrayList;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.cvm.CVMComponent;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;

public interface AdmissionControllerI 
	extends	OfferedI, RequiredI{

	public RequestDispatcher addRequestDispatcher(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestNotificationHandlerInboundPortURI) throws Exception;
	
	public ArrayList<ApplicationVM> addApplicationVMs(RequestDispatcher rd, CVMComponent cvm) throws Exception;
	
	/**
	 * Supprime le RequestDispatcher associé à l'URI du port donné en paramètre.
	 * @param RD_RequestSubmissionInboundPortUri
	 * 		Uri du port du RequestDispatcher à supprimer.
	 * @throws Exception
	 */
	public void removeRequestSource(
			String RD_RequestSubmissionInboundPortUri) throws Exception;
	
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts();
}
