package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherInboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorOutboundPort;
import fr.upmc.gaspardleo.test.DistributedTest;

public 	class 		RequestDispatcher 
		extends 	AbstractComponent 
		implements 	RequestDispatcherI, 
					RequestSubmissionHandlerI , 
					RequestNotificationHandlerI, 
					RequestNotificationI, 
					ShutdownableI {

	public static enum	RDPortTypes {
		REQUEST_SUBMISSION_IN, 
		REQUEST_SUBMISSION_OUT, 
		REQUEST_NOTIFICATION_OUT,
		REQUEST_NOTIFICATION_IN,
		REQUEST_DISPATCHER_IN,
		INTROSPECTION,
		SHUTDOWNABLE_IN,
		REQUEST_GENERATOR_MANAGER_OUT
	}
	
	private String 											Component_URI;
	
	// VMs
	private HashMap<String, RequestSubmissionOutboundPort> 		registeredVmsRsop;
	private HashMap<String, RequestNotificationInboundPort> 	registeredVmsRnip;
	private ArrayList<HashMap<ApplicationVMPortTypes, String>> 	registeredVmsUri;
	
	//Ports
	private RequestSubmissionInboundPort 					rsip;
	private RequestNotificationOutboundPort 				rnop;
	private RequestSubmissionOutboundPort 					rsop;
	private RequestNotificationInboundPort 					rnip;
	private RequestDispatcherInboundPort					rdip;
	private ShutdownableInboundPort							sip;
	private RequestGeneratorOutboundPort					rgop;
	private AdmissionControllerOutboundPort 				acop;
	private RequestGeneratorManagementOutboundPort 			rgmop;
	
	//Misc
	private Integer 										vmCursor;

	public RequestDispatcher(
			HashMap<RDPortTypes, String> component_uris, 
			HashMap<RGPortTypes, String> rg_uris,
			HashMap<ACPortTypes, String> ac_uris) throws Exception {
		
		super(1, 1);
			
		this.Component_URI 		= component_uris.get(RDPortTypes.INTROSPECTION);
		this.registeredVmsUri 	= new ArrayList<>();
		this.registeredVmsRsop 	= new HashMap<>();
		this.registeredVmsRnip	= new HashMap<>();
		this.vmCursor 			= 0;

		// Request submission inbound port connection.
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(
				component_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN), 
				this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		
		// Request submission outbound port connection.
		this.addRequiredInterface(RequestSubmissionI.class);
        this.rsop = new RequestSubmissionOutboundPort(
        		component_uris.get(RDPortTypes.REQUEST_SUBMISSION_OUT), 
        		this);
		this.addPort(this.rsop);
		this.rsop.publishPort();
				
		// Request notification submission inbound port connection.
		this.addOfferedInterface(RequestNotificationI.class);
		this.rnip = new RequestNotificationInboundPort(
				component_uris.get(RDPortTypes.REQUEST_NOTIFICATION_IN), 
				this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		
		// Request notification submission outbound port connection.
		this.addRequiredInterface(RequestNotificationI.class);
		this.rnop = new RequestNotificationOutboundPort(
				component_uris.get(RDPortTypes.REQUEST_NOTIFICATION_OUT), 
				this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.rnop.doConnection(
				rg_uris.get(RGPortTypes.REQUEST_NOTIFICATION_IN), 
				RequestNotificationConnector.class.getCanonicalName());
		
		assert component_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN) != null : "assertion : rg_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN) null";
		
		//RequestDispatcher
		this.addOfferedInterface(RequestDispatcherI.class);
		this.rdip = new RequestDispatcherInboundPort(
				component_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				this);	
		
		assert this.rdip != null : "assertion : this.rdip null";
		
		this.addPort(rdip);
		this.rdip.publishPort();
		
		// Shutdown port
		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(
				component_uris.get(RDPortTypes.SHUTDOWNABLE_IN), 
				this);
		this.addPort(this.sip);
		this.sip.publishPort();
		
		//Request Generator port
		this.addRequiredInterface(RequestGeneratorConnectionI.class);
		this.rgop = new RequestGeneratorOutboundPort(this);
		this.addPort(this.rgop);
		this.rgop.publishPort();
		this.rgop.doConnection(
				rg_uris.get(RGPortTypes.CONNECTION_IN), 
				ClassFactory.newConnector(RequestGeneratorConnectionI.class).getCanonicalName());
		this.rgop.doConnectionWithRD(
				component_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN));
		
		//Admission Crontroler port
		this.addRequiredInterface(AdmissionControllerI.class);
		this.acop = new AdmissionControllerOutboundPort(this);
		this.acop.publishPort();
		this.addPort(acop);
		this.acop.doConnection(
				ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), 
				ClassFactory.newConnector(AdmissionControllerI.class).getCanonicalName());

		// Addition by AC the new RD for a specific RG
		this.acop.addRequestDispatcher(component_uris, rg_uris);
		
		// Request Generator Management port
		rgmop = new RequestGeneratorManagementOutboundPort(
				component_uris.get(RDPortTypes.REQUEST_GENERATOR_MANAGER_OUT),
				this);
		rgmop.publishPort();
		this.addPort(rgmop);
		rgmop.doConnection(
				rg_uris.get(RGPortTypes.MANAGEMENT_IN),
				RequestGeneratorManagementConnector.class.getCanonicalName());
		
		// Execution of the main senario
		DistributedTest.testScenario(rgmop);;
		
		// Request Dispatcher debug
		this.toggleLogging();
	}

	@Override
	public String registerVM(
			HashMap<ApplicationVMPortTypes, String> avmURIs, 
			Class<?> vmInterface) throws Exception {
		String avmUri = avmURIs.get(ApplicationVMPortTypes.INTROSPECTION);
		
		// Verifi si l'AVM est déjà registered.
		if (this.registeredVmsUri.stream().anyMatch((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri))) { 
			this.logMessage("Register AVM : You just tried to register an AVM that already was registered it this RequestDispatcher.");
			return null;
		}
		
		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
		this.addPort(rsop);
		rsop.publishPort();
		
		rsop.doConnection(avmURIs.get(ApplicationVMPortTypes.REQUEST_SUBMISSION), 
				ClassFactory.newConnector(vmInterface).getCanonicalName());
		
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(this);
		this.addPort(rnip);
		rnip.publishPort();
				
		this.registeredVmsRnip.put(avmUri, rnip);
		this.registeredVmsRsop.put(avmUri, rsop);
		this.registeredVmsUri.add(avmURIs);
		
		this.logMessage(this.Component_URI + " : " + avmURIs + " has been added.");
		
		return rnip.getPortURI();
	}


	@Override
	public void unregisterVM(String vmUri) throws Exception {
		Optional<HashMap<ApplicationVMPortTypes,String>> URIs = 
				registeredVmsUri.stream()
				.filter(e -> e.get(ApplicationVMPortTypes.INTROSPECTION).equals(vmUri))
				.findFirst();
		
		if (!URIs.isPresent()) {
			this.logMessage("Unregister AVM : This AVM is not registered!");
			return;
		}
		
		registeredVmsUri.remove(URIs.get());
		registeredVmsRnip.get(vmUri).doDisconnection();
		registeredVmsRsop.get(vmUri).doDisconnection();
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		System.out.println("XXXXXXXXXXXXXXXXx Request accept !");
		this.logMessage(this.Component_URI + " : incoming request submission");
		
		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
			
		} else {
			
			String avmURI = registeredVmsUri
					.get(vmCursor%registeredVmsUri.size()).get(ApplicationVMPortTypes.INTROSPECTION);
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(
					avmURI); 

			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}

			rsop.submitRequest(r);

			vmCursor++;
		}
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		this.logMessage(this.Component_URI + " : incoming request submission and notification.");

		if (this.registeredVmsUri.size() == 0) {
			
			this.logMessage(this.Component_URI + " : no registered vm.");
			
		} else {
			
			vmCursor = (vmCursor+1) % this.registeredVmsUri.size();
			
			String avmURI = registeredVmsUri
					.get(vmCursor).get(ApplicationVMPortTypes.INTROSPECTION);
			
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(avmURI);
			
			this.logMessage(this.Component_URI + " is using " + avmURI);
			
			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}
			
			rsop.submitRequestAndNotify(r);
		}
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		this.logMessage(this.Component_URI + " : incoming request termination notification.");
		rnop.notifyRequestTermination(r);
	}
	
	@Override
	public void notifyRequestTermination(RequestI r) throws Exception {
		this.logMessage(this.Component_URI + " : incoming request termination notification.");
		// TODO Pas utilisé.
		rnop.notifyRequestTermination(r);
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		registeredVmsUri.forEach(e -> {
			String avmUri = e.get(ApplicationVMPortTypes.INTROSPECTION);
			
			try {
				registeredVmsRnip.get(avmUri).doDisconnection();
				registeredVmsRsop.get(avmUri).doDisconnection();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		registeredVmsUri.clear();
		registeredVmsRsop.clear();
		registeredVmsUri.clear();
		
		super.shutdown();
	}	
	
	public static HashMap<RDPortTypes, String> newInstance( 
			String Component_URI, 
			HashMap<RGPortTypes, String> rg_uris,
			HashMap<ACPortTypes, String> ac_uris,
			ComponentCreator cc) throws Exception {
		
		String RequestSubmission_In = AbstractPort.generatePortURI();
		String RequestSubmission_Out = AbstractPort.generatePortURI();
		String RequestNotification_In = AbstractPort.generatePortURI();
		String RequestNotification_Out = AbstractPort.generatePortURI();
		String RequestDispatcher_In = AbstractPort.generatePortURI();
		String Shutdownable_In = AbstractPort.generatePortURI();
		String RequestGeneratorManager_Out = AbstractPort.generatePortURI();
		
		HashMap<RDPortTypes, String> component_uris = new HashMap<RDPortTypes, String>() ;		
		component_uris.put(RDPortTypes.INTROSPECTION, Component_URI);
		component_uris.put(RDPortTypes.REQUEST_SUBMISSION_IN, RequestSubmission_In);
		component_uris.put(RDPortTypes.REQUEST_SUBMISSION_OUT, RequestSubmission_Out);
		component_uris.put(RDPortTypes.REQUEST_NOTIFICATION_IN, RequestNotification_In);
		component_uris.put(RDPortTypes.REQUEST_NOTIFICATION_OUT, RequestNotification_Out);
		component_uris.put(RDPortTypes.REQUEST_DISPATCHER_IN, RequestDispatcher_In);
		component_uris.put(RDPortTypes.SHUTDOWNABLE_IN, Shutdownable_In);
		component_uris.put(RDPortTypes.REQUEST_GENERATOR_MANAGER_OUT, RequestGeneratorManager_Out);
		
		Object[] constructorParams = new Object[]{ 
				component_uris,
				rg_uris,
				ac_uris
		};
		
		try {
			cc.createComponent(RequestDispatcher.class, constructorParams);
		} catch (Exception e) {
		    e.getCause().printStackTrace();
		}
		
		return component_uris;
	}
}
