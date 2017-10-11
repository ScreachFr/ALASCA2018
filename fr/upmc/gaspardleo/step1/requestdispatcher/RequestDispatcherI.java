package fr.upmc.gaspardleo.step1.requestdispatcher;

import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;

public interface RequestDispatcherI 
	extends RequestSubmissionHandlerI, RequestNotificationI{

	public void registerVM(String vmUri) throws Exception;
	public void unregisterVM(String vmUri) throws Exception;
}
