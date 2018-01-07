package fr.upmc.gaspardleo.requestdispatcher.connectors;

import java.util.List;
import java.util.Map;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;

public class RequestDispatherConnector 
	extends AbstractConnector
	implements RequestDispatcherI{

	@Override
	public String registerVM(Map<ApplicationVMPortTypes, String> avmURIs, Class<?> vmInterface) throws Exception {
		return ((RequestDispatcherI)this.offering).registerVM(avmURIs, vmInterface);		
	}

	@Override
	public void unregisterVM(String vmUri) throws Exception {
		((RequestDispatcherI)this.offering).unregisterVM(vmUri);
	}
	
	@Override
	public void unregisterVM() throws Exception {
		((RequestDispatcherI)this.offering).unregisterVM();
	}

	@Override
	public List<String> getRegisteredAVMUris() throws Exception {
		return ((RequestDispatcherI)this.offering).getRegisteredAVMUris();
	}
}
