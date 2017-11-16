package fr.upmc.gaspardleo.admissioncontroller.interfaces;

public interface AdmissionControllerI {

	public String addRequestSource(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI) throws Exception;
	
	/**
	 * Supprime le RequestDispatcher associé à l'URI du port donné en paramètre.
	 * @param RD_RequestSubmissionInboundPortUri
	 * 		Uri du port du RequestDispatcher à supprimer.
	 * @throws Exception
	 */
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception;
}
