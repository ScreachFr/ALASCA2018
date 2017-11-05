package fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces;

public interface AdmissionControllerI {

	public String addRequestSource(
					String RG_RequestNotificationInboundPortURI,
					String CVM_InboundPorURI) throws Exception;
}
