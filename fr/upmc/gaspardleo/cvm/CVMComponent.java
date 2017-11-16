package fr.upmc.gaspardleo.cvm;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.cvm.interfaces.CVMI;
import fr.upmc.gaspardleo.cvm.ports.CVMInboundPort;

public class CVMComponent extends AbstractComponent implements CVMI {
	
	public enum CVMPortTypes {
		INTROSPECTION;
	}
	
	private CVM realCVM;
	
	private CVMInboundPort cvmip;
	
	public CVMComponent(CVM realCVM) throws Exception {
		super(1, 1);
		this.realCVM = realCVM;
		
		this.cvmip = new CVMInboundPort(this);
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
	
	@Override
	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) {
		this.realCVM.addAVMPort(avmPort);
	}
	
	@Override
	public void addPort(PortI cmp) throws Exception{
		super.addPort(cmp);
	}
	
	public Map<CVMPortTypes, String> getCVMPortsURI() throws Exception {
		Map<CVMPortTypes, String> result = new HashMap<>();
		
		result.put(CVMPortTypes.INTROSPECTION, this.cvmip.getPortURI());
		
		return result;
	}
}
