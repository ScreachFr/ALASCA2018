package fr.upmc.gaspardleo.admissioncontroller.port;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class AdmissionControllerInboundPort extends AbstractInboundPort
implements AdmissionControllerI {

	private static final long serialVersionUID = -476427438292215937L;

	public AdmissionControllerInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
	}

	@Override
	public void addRequestDispatcher(
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris,
			String rg_monitor_in) throws Exception {
		
		AdmissionController ac = (AdmissionController) this.owner;

		ac.handleRequestAsync(
				new ComponentI.ComponentService<AdmissionController>(){
					@Override
					public AdmissionController call() throws Exception {
						ac.addRequestDispatcher(
								RD_uris, 
								RG_uris,
								rg_monitor_in);
						return ac;
					}
				});
	}

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

}
