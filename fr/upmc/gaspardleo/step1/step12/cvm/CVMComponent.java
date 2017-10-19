package fr.upmc.gaspardleo.step1.step12.cvm;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;
import fr.upmc.gaspardleo.step1.step12.cvm.ports.CVMInboundPort;

public class CVMComponent extends AbstractComponent implements CVMI {
	private AbstractCVM realCVM;
	
	private CVMInboundPort cvmip;
	
	public CVMComponent(AbstractCVM realCVM, String cvmipUri) throws Exception {
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

}
