package fr.upmc.gaspardleo.admissioncontroller.connectors;

import java.util.ArrayList;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.cvm.CVMComponent;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;

public class AdmissionControllerConnector 
		extends AbstractConnector
		implements AdmissionControllerI{

	@Override
	public RequestDispatcher addRequestDispatcher(
			String RD_URI,
			String RG_RequestNotificationInboundPortURI,
			String RG_RequestNotificationHandlerInboundPortURI) throws Exception {
		return ((AdmissionControllerI)this.offering).addRequestDispatcher(
				RD_URI,
				RG_RequestNotificationInboundPortURI,
				RG_RequestNotificationHandlerInboundPortURI);
	}

	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		((AdmissionControllerI)this.offering).removeRequestSource(RD_RequestSubmissionInboundPortUri);
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {
		return ((AdmissionControllerI)this.offering).getApplicationVMManagementOutboundPorts();
	}

	@Override
	public ArrayList<ApplicationVM> addApplicationVMs(RequestDispatcher rd, CVMComponent cvm) throws Exception {
		return ((AdmissionControllerI)this.offering).addApplicationVMs(rd, cvm);
	}
}
