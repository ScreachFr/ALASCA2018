package fr.upmc.gaspardleo.admissioncontroller.port;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerOutboundPort 
	extends AbstractOutboundPort
	implements AdmissionControllerI{

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
	}

	@Override
	public String addRequestSource(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI) throws Exception { 
		
		return ((AdmissionControllerI)this.connector).addRequestSource(
				RD_URI,
				RG_RequestNotificationInboundPortURI);	
	}
}
