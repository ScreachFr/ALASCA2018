package fr.upmc.gaspardleo.step1.requestdispatcher;

import java.util.HashSet;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;

public class RequestDispatcher 
	extends AbstractComponent 
	implements RequestDispatcherI, RequestSubmissionHandlerI{
	
	private String dispatcherUri;
	private Set<String> registeredVms;
	
	
	public RequestDispatcher(String dispatcherUri) {
		super(1, 1);
		
		this.dispatcherUri = dispatcherUri;
		this.registeredVms = new HashSet<>();
	}
	
	public void registerVM(String vmUri) throws Exception {
		registeredVms.add(vmUri);
	}
	
	public void unregisterVM(String vmUri) throws Exception {
		registeredVms.remove(vmUri);
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
	}	
}
