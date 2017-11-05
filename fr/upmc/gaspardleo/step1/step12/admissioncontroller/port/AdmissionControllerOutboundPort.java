package fr.upmc.gaspardleo.step1.step12.admissioncontroller.port;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerOutboundPort 
	extends AbstractOutboundPort
	implements AdmissionControllerI{

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
	}

	@Override
	public String addRequestSource(
					String RG_RequestNotificationInboundPortURI,
					String CVM_InboundPorURI) throws Exception { 
		
		return ((AdmissionControllerI)this.connector).addRequestSource(
						RG_RequestNotificationInboundPortURI,
						CVM_InboundPorURI);	
	}
}
