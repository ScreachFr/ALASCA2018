package fr.upmc.gaspardleo.step1.step11.requestdispatcher.interfaces;

public interface RequestDispatcherI{

	public void registerVM(String vmUri, String requestSubmissionOutboundPort) throws Exception;
	public void unregisterVM(String vmUri) throws Exception;
}
