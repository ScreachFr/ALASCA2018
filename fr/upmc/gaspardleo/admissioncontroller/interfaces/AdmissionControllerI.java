package fr.upmc.gaspardleo.admissioncontroller.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public interface AdmissionControllerI 
	extends	OfferedI, RequiredI{

	public void addRequestDispatcher(
		HashMap<RDPortTypes, String> RD_uris,
		HashMap<RGPortTypes, String> RG_uris,
		String rg_monitor_in) throws Exception;
		
	/**
	 * Supprime le RequestDispatcher associé à l'URI du port donné en paramètre.
	 * @param RD_RequestSubmissionInboundPortUri
	 * 		Uri du port du RequestDispatcher à supprimer.
	 * @throws Exception
	 */
	public void removeRequestSource(String requestGeneratorURI) throws Exception;
	
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() throws Exception;
	
	public void createNewRequestDispatcher(
			int num_rd, 
			HashMap<RGPortTypes, String> rg_uris, 
			HashMap<ACPortTypes, String> ac_uris) throws Exception;
}
