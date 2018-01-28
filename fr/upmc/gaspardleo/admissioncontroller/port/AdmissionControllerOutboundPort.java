package fr.upmc.gaspardleo.admissioncontroller.port;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class AdmissionControllerOutboundPort 
		extends AbstractOutboundPort
		implements AdmissionControllerI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
		assert	uri != null ;
	}
	
	public AdmissionControllerOutboundPort(ComponentI owner) throws Exception {
		super(AbstractPort.generatePortURI(), AdmissionControllerI.class, owner);
		assert	uri != null ;
	}

	@Override
	public void addRequestDispatcher(
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris,
			String rg_monitor_in) throws Exception { 
		
		((AdmissionControllerI)this.connector).addRequestDispatcher(
				RD_uris, 
				RG_uris,
				rg_monitor_in);	
	}

	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		((AdmissionControllerI)this.connector).removeRequestSource(RD_RequestSubmissionInboundPortUri);
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() throws Exception {
		return ((AdmissionControllerI)this.connector).getApplicationVMManagementOutboundPorts();
	}

	@Override
	public void createNewRequestDispatcher(int num_rd, HashMap<RGPortTypes, String> rg_uris,
			HashMap<ACPortTypes, String> ac_uris) throws Exception {
		((AdmissionControllerI)this.connector).createNewRequestDispatcher(num_rd, rg_uris, ac_uris);
	}
}
