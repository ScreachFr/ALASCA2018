package fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces;

public interface AdmissionControllerI {

	public void addRequestSource(
			String RequestSubmissionOutboundPortURI, 
			String RequestNotificationInboundPortURI,
			String RequestGeneratorManagementInboundPortURI) throws Exception;
}
