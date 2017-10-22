package fr.upmc.gaspardleo.step1.step12.cvm.connectors;

import fr.upmc.components.ComponentI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;

public class CVMConnector extends AbstractConnector implements CVMI {
	
	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		((CVMI)this.offering).deployComponent(cmp);
		
	}

	@Override
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		((CVMI)this.offering).allocateCores(avmPort);
	}

	@Override
	public void start() throws Exception {
		((CVMI)this.offering).start();
	}

	@Override
	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		((CVMI)this.offering).addAVMPort(avmPort);
	}


}
