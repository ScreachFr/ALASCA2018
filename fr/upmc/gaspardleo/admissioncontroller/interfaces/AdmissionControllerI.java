package fr.upmc.gaspardleo.admissioncontroller.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

/**
 * L'interface <code> AdmissionControllerI </ code> définit le comportement de l'objet AdmissionControler
 * pour la création et la suppression de ressources pour le traitement des requêtes.
 * 
 * @author Leonor & Alexandre
 */
public 	interface 	AdmissionControllerI 
		extends		OfferedI, 
					RequiredI {

	/**
	 * Créé les composants RequestMonitor et PerformanceRegulator nécessaire ua traitement des requêtes.
	 * Connecte ces nouveaux commposant au RequestDispatcher et au RequestGenerator gâce aux Uris données en paramètre.
	 * @param 	RD_uris 	URIs du RequestDispatcher.
	 * @param 	RG_uris 	URIs du RequestGenerator.
	 * @throws 	Exception
	 */
	public void addRequestSource(
		Integer howManyAVMsOnStartup,
		HashMap<RDPortTypes, String> RD_uris,
		HashMap<RGPortTypes, String> RG_uris,
		String rg_monitor_in) throws Exception;
		
	/**
	 * Supprime le RequestDispatcher associé à l'URI du port donné en paramètre.
	 * @param 	RD_RequestSubmissionInboundPortUri 	Uri du port du RequestDispatcher à supprimer.
	 * @throws 	Exception
	 */
	public void removeRequestSource(String requestGeneratorURI) throws Exception;
	
	/**
	 * Retourne les outbound ports de management des machines virtuelles d'application.
	 * @throws Exception
	 * @retunr la liste des outbound ports de management des machines virtuelles d'application.
	 */
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() 
			throws Exception;
	
	/**
	 * Crée un composant RequestDispatcher avec les URIs données en paramètre. 
	 * @param 	num_rd		Numéro du RequestDispatcher.
	 * @param	rg_uris		URIs du RequestDispatcher.
	 * @param 	ac_uris		Uris du Composant AdmissionControler.
	 * @throws 	Exception
	 * @retunr 	la liste des outbound ports de management des machines virtuelles d'application.
	 */
	public void createNewRequestDispatcher(
			Integer num_rd,
			HashMap<RGPortTypes, String> rg_uris, 
			HashMap<ACPortTypes, String> ac_uris) throws Exception;
}
