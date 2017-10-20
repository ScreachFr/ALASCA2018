package fr.upmc.gaspardleo.step1.step12.cvm.interfaces;

import fr.upmc.components.ComponentI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public interface CVMI {

	public void deployComponent(ComponentI cmp) throws Exception;
	
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception;
}
