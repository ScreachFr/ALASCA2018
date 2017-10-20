package fr.upmc.gaspardleo.step1.step12.admissioncontroller.port;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerOutboundPort 
	extends AbstractOutboundPort
	implements AdmissionControllerI{

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
	}

	@Override
	public String addRequestSource(
			String RG_RequestSubmissionOutboundPortURI,
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestGeneratorManagementInboundPortURI,
			String CVM_IPURI) throws Exception { 
		
		return ((AdmissionControllerI)this.connector).addRequestSource(
				RG_RequestSubmissionOutboundPortURI, 
				RG_RequestNotificationInboundPortURI, 
				RG_RequestGeneratorManagementInboundPortURI, 
				CVM_IPURI);	
	}
}
