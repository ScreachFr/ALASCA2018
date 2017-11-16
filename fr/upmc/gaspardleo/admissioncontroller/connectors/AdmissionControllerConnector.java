package fr.upmc.gaspardleo.admissioncontroller.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerConnector 
		extends AbstractConnector
		implements AdmissionControllerI{

	@Override
	public String addRequestSource(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI) throws Exception {
		return ((AdmissionControllerI)this.offering).addRequestSource(
				RD_URI,
				RG_RequestNotificationInboundPortURI);
	}
}
