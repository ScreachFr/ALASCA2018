package fr.upmc.gaspardleo.admissioncontroller.connectors;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class AdmissionControllerConnector 
		extends AbstractConnector
		implements AdmissionControllerI{


	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		((AdmissionControllerI)this.offering).removeRequestSource(RD_RequestSubmissionInboundPortUri);
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() throws Exception {
		return ((AdmissionControllerI)this.offering).getApplicationVMManagementOutboundPorts();
	}

	@Override
	public void addRequestDispatcher(
			HashMap<RDPortTypes, String> RD_uris, 
			HashMap<RGPortTypes, String> RG_uris,
			String rg_monitor_in)
			throws Exception {
		((AdmissionControllerI)this.offering).addRequestDispatcher(RD_uris, RG_uris, rg_monitor_in);
	}
}
