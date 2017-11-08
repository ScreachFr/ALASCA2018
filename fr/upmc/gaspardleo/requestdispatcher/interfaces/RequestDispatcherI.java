package fr.upmc.gaspardleo.requestdispatcher.interfaces;

public interface RequestDispatcherI{

	public void registerVM(String vmUri, String requestSubmissionOutboundPort) throws Exception;
	public void unregisterVM(String vmUri) throws Exception;
}
