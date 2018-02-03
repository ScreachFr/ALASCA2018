package fr.upmc.gaspardleo.admissioncontroller.port;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

/**
 * La classe <code> AdmissionControllerInboundPort </ code> implémente le port entrant 
 * offrant l'interface <code> AdmissionControllerI </ code>.
 * @author Leonor & Alexandre
 */
public class AdmissionControllerInboundPort 
		extends AbstractInboundPort
		implements AdmissionControllerI {

	private static final long serialVersionUID = -476427438292215937L;

	/**
	 * @param 	uri			URI de l'inbound port
	 * @param 	owner		Composant propriétaire du port
	 * @throws 	Exception
	 */
	public AdmissionControllerInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
	}

	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces#addRequestSource(HashMap<RDPortTypes, String>, HashMap<RGPortTypes, String>, String)
	 */
	@Override
	public void addRequestSource(
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris,
			String rg_monitor_in) throws Exception {
		AdmissionController ac = (AdmissionController) this.owner;
		ac.handleRequestAsync(
				new ComponentI.ComponentService<AdmissionController>(){
					@Override
					public AdmissionController call() throws Exception {
						ac.addRequestSource(
								RD_uris, 
								RG_uris,
								rg_monitor_in);
						return ac;
					}
				});
	}
	
	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces#removeRequestSource(String)
	 */
	@Override
	public void removeRequestSource(String requestGeneratorURI) throws Exception {
		AdmissionController ac = (AdmissionController) this.owner;
		ac.handleRequestAsync(
				new ComponentI.ComponentService<AdmissionController>(){
					@Override
					public AdmissionController call() throws Exception {
						ac.removeRequestSource(requestGeneratorURI);
						return ac;
					}
				});
	}

	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces#getApplicationVMManagementOutboundPorts()
	 */
	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() throws Exception {
		AdmissionController ac = (AdmissionController) this.owner;
		return ac.handleRequestSync(
				new ComponentI.ComponentService<ArrayList<ApplicationVMManagementOutboundPort>>(){
					@Override
					public ArrayList<ApplicationVMManagementOutboundPort> call() throws Exception {
						return ac.getApplicationVMManagementOutboundPorts();
					}
				});
	}

	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces#createNewRequestDispatcher(int, HashMap<RGPortTypes, String>, HashMap<ACPortTypes, String>)
	 */
	@Override
	public void createNewRequestDispatcher(int num_rd, HashMap<RGPortTypes, String> rg_uris,
			HashMap<ACPortTypes, String> ac_uris) throws Exception {
		AdmissionController ac = (AdmissionController) this.owner;
		ac.handleRequestAsync(
				new ComponentI.ComponentService<AdmissionController>(){
					@Override
					public AdmissionController call() throws Exception {
						ac.createNewRequestDispatcher(num_rd, rg_uris, ac_uris);
						return ac;
					}
				});
	}
}
