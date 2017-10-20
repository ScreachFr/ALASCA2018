package fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces;

public interface AdmissionControllerI {

	public String addRequestSource(
			String RG_RequestSubmissionOutboundPortURI,
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestGeneratorManagementInboundPortURI,
			String CVM_IPURI) throws Exception;
}
