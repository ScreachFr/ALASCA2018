package fr.upmc.gaspardleo.admissioncontroller.port;

import java.util.ArrayList;
import java.util.Map;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class AdmissionControllerOutboundPort 
	extends AbstractOutboundPort
	implements AdmissionControllerI{

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
		
		assert	uri != null ;
	}

	@Override
	public void addRequestDispatcher(
			String RD_URI,
			Map<RGPortTypes, String> requestGeneratorURIs) throws Exception { 
		
		((AdmissionControllerI)this.connector).addRequestDispatcher(
				RD_URI,
				requestGeneratorURIs);	
	}

	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		((AdmissionControllerI)this.connector).removeRequestSource(RD_RequestSubmissionInboundPortUri);
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() throws Exception {
		return ((AdmissionControllerI)this.connector).getApplicationVMManagementOutboundPorts();
	}
}
