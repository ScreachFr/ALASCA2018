package fr.upmc.gaspardleo.applicationvm;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.ports.AbstractPort;

public class ApplicationVM extends fr.upmc.datacenter.software.applicationvm.ApplicationVM{

	private String vmURI;
	public ApplicationVM(String vmURI) throws Exception {
		super(
			vmURI, 
			AbstractPort.generatePortURI(), 
			AbstractPort.generatePortURI(),
			AbstractPort.generatePortURI());
		this.vmURI = vmURI;
	}
	
	@Override
	public Map<ApplicationVMPortTypes, String>	getAVMPortsURI() throws Exception {
		HashMap<ApplicationVMPortTypes, String> ret =
						new HashMap<ApplicationVMPortTypes, String>();		
		ret.put(ApplicationVMPortTypes.REQUEST_SUBMISSION,
						this.requestSubmissionInboundPort.getPortURI());
		ret.put(ApplicationVMPortTypes.MANAGEMENT,
						this.applicationVMManagementInboundPort.getPortURI());
		ret.put(ApplicationVMPortTypes.INTROSPECTION,
				this.vmURI);
		return ret;
	}

}
