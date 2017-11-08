package fr.upmc.gaspardleo.admissioncontroller.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerConnection 
		extends AbstractConnector
		implements AdmissionControllerI{

	@Override
	public String addRequestSource(
					String RG_RequestNotificationInboundPortURI,
					String CVM_InboundPorURI) throws Exception {
		return ((AdmissionControllerI)this.offering).addRequestSource(
				RG_RequestNotificationInboundPortURI,
				CVM_InboundPorURI);
	}
}
