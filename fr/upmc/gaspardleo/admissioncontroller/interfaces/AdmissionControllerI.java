package fr.upmc.gaspardleo.admissioncontroller.interfaces;

import java.util.ArrayList;
import java.util.Map;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public interface AdmissionControllerI 
	extends	OfferedI, RequiredI{

	public void addRequestDispatcher(
			String RD_URI,
			Map<RGPortTypes, String> requestGeneratorURIs) throws Exception;
		
	/**
	 * Supprime le RequestDispatcher associé à l'URI du port donné en paramètre.
	 * @param RD_RequestSubmissionInboundPortUri
	 * 		Uri du port du RequestDispatcher à supprimer.
	 * @throws Exception
	 */
	public void removeRequestSource(
			String requestGeneratorURI) throws Exception;
	
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts();
}
