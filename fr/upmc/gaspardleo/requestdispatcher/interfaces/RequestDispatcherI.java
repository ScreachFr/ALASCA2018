package fr.upmc.gaspardleo.requestdispatcher.interfaces;

public interface RequestDispatcherI{

	/**
	 * Connect une ApplicationVM au RequestDispatcher.
	 * @param vmUri
	 * 		Uri de l'AVM.
	 * @param requestSubmissionOutboundPort
	 * 		Uri du port de soumission des requètes.
	 * @return
	 * 		Uri du port de notification de terminaison des requètes.
	 * @throws Exception
	 */
	public String registerVM(String vmUri, 
			String requestSubmissionOutboundPort) throws Exception;
	
	public void unregisterVM(String vmUri) throws Exception;
}
