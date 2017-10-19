package fr.upmc.gaspardleo.step1.step12.admissioncontroller.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerConnection 
		extends AbstractConnector
		implements AdmissionControllerI{

	@Override
	public void addRequestSource(
			String RG_RequestSubmissionOutboundPortURI, 
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestGeneratorManagementInboundPortURI,
			String CVM_IPURI) throws Exception {
		((AdmissionControllerI)this.offering).addRequestSource(
				RG_RequestSubmissionOutboundPortURI,
				RG_RequestNotificationInboundPortURI,
				RG_RequestGeneratorManagementInboundPortURI,
				CVM_IPURI);
	}
}
