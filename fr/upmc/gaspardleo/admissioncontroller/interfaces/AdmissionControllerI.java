package fr.upmc.gaspardleo.admissioncontroller.interfaces;

public interface AdmissionControllerI {

	public String addRequestSource(
					String RG_RequestNotificationInboundPortURI,
					String CVM_InboundPorURI) throws Exception;
}
