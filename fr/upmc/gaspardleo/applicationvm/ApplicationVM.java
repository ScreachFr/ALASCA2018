package fr.upmc.gaspardleo.applicationvm;

import java.util.HashMap;
import java.util.Map;

public class ApplicationVM extends fr.upmc.datacenter.software.applicationvm.ApplicationVM{

	//TODO Constructeur sans URI et utilisation de AbstractURI directement dans le code du constructeur
	
	public ApplicationVM(String vmURI, String applicationVMManagementInboundPortURI,
			String requestSubmissionInboundPortURI, String requestNotificationOutboundPortURI) throws Exception {
		super(vmURI, applicationVMManagementInboundPortURI, requestSubmissionInboundPortURI,
				requestNotificationOutboundPortURI);
	}
	
	public Map<ApplicationVMPortTypes, String>	getAVMPortsURI() throws Exception {
		HashMap<ApplicationVMPortTypes, String> ret =
						new HashMap<ApplicationVMPortTypes, String>();		
		ret.put(ApplicationVMPortTypes.REQUEST_SUBMISSION,
						this.requestSubmissionInboundPort.getPortURI());
		ret.put(ApplicationVMPortTypes.MANAGEMENT,
						this.applicationVMManagementInboundPort.getPortURI());
		return ret;
	}

}
