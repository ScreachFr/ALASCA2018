package fr.upmc.gaspardleo.admissioncontroller.port;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;

public class AdmissionControllerOutboundPort 
	extends AbstractOutboundPort
	implements AdmissionControllerI{

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
		
//		if (uri == null){
//			System.out.println("uri NULL");
//		}
		
		assert	uri != null ;
	}

	@Override
	public void addRequestDispatcher(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI/*,
			String RG_RequestNotificationHandlerInboundPortURI*/) throws Exception { 
		
		((AdmissionControllerI)this.connector).addRequestDispatcher(
				RD_URI,
				RG_RequestNotificationInboundPortURI/*,
				RG_RequestNotificationHandlerInboundPortURI*/);	
	}

	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		((AdmissionControllerI)this.connector).removeRequestSource(RD_RequestSubmissionInboundPortUri);
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {
		return ((AdmissionControllerI)this.connector).getApplicationVMManagementOutboundPorts();
	}
}
