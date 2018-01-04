package fr.upmc.gaspardleo.requestdispatcher.interfaces;

import java.util.Map;

import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;

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
	public String registerVM(Map<ApplicationVMPortTypes, String> avmURIs, Class<?> vmInterface) throws Exception;
	
	public void unregisterVM(String vmUri) throws Exception;
	public void unregisterVM() throws Exception;
}
