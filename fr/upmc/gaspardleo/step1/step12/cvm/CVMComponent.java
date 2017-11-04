package fr.upmc.gaspardleo.step1.step12.cvm;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.PortI;
import fr.upmc.gaspardleo.step0.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;
import fr.upmc.gaspardleo.step1.step12.cvm.ports.CVMInboundPort;

public class CVMComponent extends AbstractComponent implements CVMI {
	private CVM realCVM;
	
	private CVMInboundPort cvmip;
	
	public CVMComponent(CVM realCVM, String cvmipUri) throws Exception {
		super(1, 1);
		this.realCVM = realCVM;
		
		this.cvmip = new CVMInboundPort(cvmipUri, this);
		this.addPort(cvmip);
		this.cvmip.publishPort();
		
		this.addOfferedInterface(CVMI.class);
	}
	
	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		realCVM.addDeployedComponent(cmp);
	}

	@Override
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		realCVM.allocateCores(avmPort);
	}
	
	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) {
		this.realCVM.addAVMPort(avmPort);
	}
	
	public void addPort(PortI cmp) throws Exception{
		super.addPort(cmp);
	}
}
