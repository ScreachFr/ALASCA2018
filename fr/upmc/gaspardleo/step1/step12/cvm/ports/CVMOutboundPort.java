package fr.upmc.gaspardleo.step1.step12.cvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.step0.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;

public class CVMOutboundPort extends AbstractOutboundPort implements CVMI {

	public CVMOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CVMI.class, owner);
	}
	
	public CVMOutboundPort(ComponentI owner) throws Exception {
		super(CVMI.class, owner);
	}

	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		((CVMI)this.connector).deployComponent(cmp);
	}

	@Override
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {		
		((CVMI)this.connector).allocateCores(avmPort);
	}

	@Override
	public void start() throws Exception {
		((CVMI)this.connector).start();
	}

	@Override
	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		((CVMI)this.connector).addAVMPort(avmPort);
	}

}
