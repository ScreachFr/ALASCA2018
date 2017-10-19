package fr.upmc.gaspardleo.step1.step12.admissioncontroller.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerConnection 
		extends AbstractConnector
		implements AdmissionControllerI{

	@Override
	public void addRequestSource(
			String RequestSubmissionOutboundPortURI, 
			String RequestNotificationInboundPortURI,
			String RequestGeneratorManagementInboundPortURI) throws Exception {
		((AdmissionControllerI)this.offering).addRequestSource(
				RequestSubmissionOutboundPortURI,
				RequestNotificationInboundPortURI,
				RequestGeneratorManagementInboundPortURI);
	}
}
