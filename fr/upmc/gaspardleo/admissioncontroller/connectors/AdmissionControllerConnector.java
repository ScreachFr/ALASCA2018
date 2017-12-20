package fr.upmc.gaspardleo.admissioncontroller.connectors;

import java.util.ArrayList;
import java.util.Map;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class AdmissionControllerConnector 
		extends AbstractConnector
		implements AdmissionControllerI{

	@Override
	public void addRequestDispatcher(
			String RD_URI,
			Map<RGPortTypes, String> requestGeneratorURIs) throws Exception {
		
//		System.out.println("[DEBUG LEO] (AdmissionControllerI)this.offering " + (AdmissionControllerI)this.offering);
		
		((AdmissionControllerI)this.offering).addRequestDispatcher(
				RD_URI,
				requestGeneratorURIs);
	}

	@Override
	public void removeRequestSource(String RD_RequestSubmissionInboundPortUri) throws Exception {
		((AdmissionControllerI)this.offering).removeRequestSource(RD_RequestSubmissionInboundPortUri);
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() throws Exception {
		return ((AdmissionControllerI)this.offering).getApplicationVMManagementOutboundPorts();
	}
}
