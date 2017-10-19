package fr.upmc.gaspardleo.step1.step12.admissioncontroller.port;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.step1.step12.admissioncontroller.interfaces.AdmissionControllerI;

public class AdmissionControllerOutboundPort 
	extends AbstractOutboundPort
	implements AdmissionControllerI{

	public AdmissionControllerOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, AdmissionControllerI.class, owner);
	}

	@Override
	public void addRequestSource(
			String RequestSubmissionOutboundPortURI, 
			String RequestNotificationInboundPortURI,
			String RequestGeneratorManagementInboundPortURI) throws Exception {
		final AdmissionController admissionController = (AdmissionController)this.owner;
		admissionController.handleRequestAsync(
				new ComponentI.ComponentService<AdmissionController>(){
					@Override
					public AdmissionController call() throws Exception {
						admissionController.addRequestSource(
								RequestSubmissionOutboundPortURI,
								RequestNotificationInboundPortURI,
								RequestGeneratorManagementInboundPortURI);
						return admissionController;
					}});
	}
}
