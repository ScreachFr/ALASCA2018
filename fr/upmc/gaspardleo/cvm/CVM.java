package fr.upmc.gaspardleo.cvm;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class CVM extends AbstractCVM {
	
		
	private AllocatedCore[] 							cores;
	private int 										currentCore;
	
	public CVM() throws Exception {		
		super();
				
		this.currentCore = 0;
	}

	@Override
	public void deploy() throws Exception {
		
		AbstractComponent.configureLogging("", "", 0, '|');
		Processor.DEBUG = true;
		super.deploy();
	}
	
	@Override
	public void start() throws Exception {
		
		super.start();
	}

	private AllocatedCore[] getAllocatedCore() {
		
		AllocatedCore[] result = new AllocatedCore[1];
		
		result[0] = this.cores[this.currentCore];
		
		this.currentCore = (this.currentCore + 1) % this.cores.length;
				
		return result;
	}
	
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		
		avmPort.allocateCores(getAllocatedCore()) ;
	}
}
