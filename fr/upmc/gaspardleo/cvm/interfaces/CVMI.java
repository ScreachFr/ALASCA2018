package fr.upmc.gaspardleo.cvm.interfaces;

import fr.upmc.components.ComponentI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public interface CVMI {

	public void deployComponent(ComponentI cmp) throws Exception;
	
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception;
	
	public void start() throws Exception;
	
	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) throws Exception;
	
}
