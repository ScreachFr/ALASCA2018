package fr.upmc.gaspardleo.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionOutboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherInboundPort;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

/**
 * La classe <code> RequestDispatcher </ code> implémente le composant représentant 
 * le répartiteur de requêts dans le centre de calcul.
 * 
 * @author Leonor & Alexandre
 */
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
		REQUEST_GENERATOR_MANAGER_OUT,
		RQUEST_MONITOR_IN
	}
	
	/** URI du composant */
	private String Component_URI;
	/** Liste de URIs et outbound ports utilisés pour la soumission de request */
	private HashMap<String, RequestSubmissionOutboundPort> registeredVmsRsop;
	/** Liste de URIs et outbound ports utilisés pour les notifications de request */
	private HashMap<String, RequestNotificationInboundPort> registeredVmsRnip;
	/** Liste des URIS des applications VM enregistrées */
	private ArrayList<HashMap<ApplicationVMPortTypes, String>> registeredVmsUri;
	/** Outbound port pour utiliser les services de soumission de requêts */
	private RequestSubmissionInboundPort rsip;
	/** URI de l'outbound port de notifictaion de requêtes */
	private String rnop_uri;
	/** URIs du RequestGenerator */
	private HashMap<RGPortTypes, String> rg_uris;
	/** Inbound port offrant les services de notification de requêtes */
	private RequestNotificationInboundPort rnip;
	/** Inbound port offrant les services du composant */
	private RequestDispatcherInboundPort rdip;
	/** Inbound port offrant les services d'arrêts d'autres composants tel que les applications VM */
	private ShutdownableInboundPort sip;
	/** Outbound port pour utiliser les service du contrôller d'admission */
	private AdmissionControllerOutboundPort acop;
	/** Intérateur sur les applications VM pour la soumission des requêtes */
	private Integer vmCursor;
	/** URI de l'outbound port pour utiliser les services du RequestMonitor */
	private String rmop_uri;
	
	/**
	 * @param 	component_uris	URIs du composant
	 * @param 	rg_uris			URIs du RequestGenerator
	 * @param 	ac_uris			URIs de l'AdmissionControler
	 * @throws 	Exception
	 */
	public RequestDispatcher(
		 	HashMap<RDPortTypes, String> component_uris, 
			HashMap<RGPortTypes, String> rg_uris,
			HashMap<ACPortTypes, String> ac_uris) throws Exception {
		
		super(1, 1);
			
		this.Component_URI = component_uris.get(RDPortTypes.INTROSPECTION);
		this.registeredVmsUri = new ArrayList<>();
		this.registeredVmsRsop = new HashMap<>();
		this.registeredVmsRnip = new HashMap<>();
		this.vmCursor = 0;

		// Request submission inbound port connection.
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(component_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN), this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		
		// Request notification submission inbound port connection.
		this.addOfferedInterface(RequestNotificationI.class);
		this.rnip = new RequestNotificationInboundPort(component_uris.get(RDPortTypes.REQUEST_NOTIFICATION_IN), this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		
		this.rnop_uri = component_uris.get(RDPortTypes.REQUEST_NOTIFICATION_OUT);
		
		this.rg_uris = rg_uris;
		
		//RequestDispatcher
		this.addOfferedInterface(RequestDispatcherI.class);
		this.rdip = new RequestDispatcherInboundPort(component_uris.get(RDPortTypes.REQUEST_DISPATCHER_IN), this);	
		this.addPort(rdip);
		this.rdip.publishPort();
		
		// Shutdown port
		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(component_uris.get(RDPortTypes.SHUTDOWNABLE_IN), this);
		this.addPort(this.sip);
		this.sip.publishPort();
		
		//Admission Crontroler port
		this.addRequiredInterface(AdmissionControllerI.class);
		this.acop = new AdmissionControllerOutboundPort(this);
		this.acop.publishPort();
		this.addPort(acop);
		
		this.acop.doConnection(
				ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), 
				ClassFactory.newConnector(AdmissionControllerI.class).getCanonicalName());
		
		this.rmop_uri = component_uris.get(RDPortTypes.RQUEST_MONITOR_IN);
		
		 //Addition by AC the new RD for a specific RG
		this.acop.addRequestSource(component_uris, rg_uris, this.rmop_uri);
		
		// Request Dispatcher debug
		this.toggleLogging();
		this.toggleTracing();
		this.logMessage("RequestDispatcher made");
	}
	
	/**
	 * @return L'URI de la prochaine application VM pour la soumission
	 */
	private synchronized String getNextVmUriFromCursor() {
		return registeredVmsUri.get(vmCursor++%registeredVmsUri.size()).get(ApplicationVMPortTypes.INTROSPECTION);
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#registerVM(final HashMap<ApplicationVMPortTypes, String>, Class<?>)
	 */
	@Override
	public String registerVM(
			HashMap<ApplicationVMPortTypes, String> avmURIs, 
			Class<?> vmInterface) throws Exception {
		
		this.logMessage("Register avm : " + avmURIs + "...");
		
		String avmUri = avmURIs.get(ApplicationVMPortTypes.INTROSPECTION);
		
		// Verifi si l'AVM est déjà registered.
		if (this.registeredVmsUri.stream().anyMatch((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION).equals(avmUri))) { 
			this.logMessage("Register AVM : You just tried to register an AVM that already was registered it this RequestDispatcher.");
			return null;
		}
	
		if(!this.isRequiredInterface(RequestSubmissionI.class))
			this.addRequiredInterface(RequestSubmissionI.class);
		
		RequestSubmissionOutboundPort rsop = new RequestSubmissionOutboundPort(this);
		this.addPort(rsop);
		rsop.publishPort();
		
		rsop.doConnection(avmURIs.get(
			ApplicationVMPortTypes.REQUEST_SUBMISSION), 
			ClassFactory.newConnector(RequestSubmissionI.class).getCanonicalName());
		
		if(!this.isRequiredInterface(RequestNotificationI.class))
			this.addRequiredInterface(RequestNotificationI.class);
		
		RequestNotificationInboundPort rnip = new RequestNotificationInboundPort(this);
		this.addPort(rnip);
		rnip.publishPort();
		
		this.registeredVmsRnip.put(avmUri, rnip);
		
		doAVMRequestNotificationAndMonitoringConnection(
			avmURIs.get(ApplicationVMPortTypes.CONNECTION_REQUEST),
			this.rnip.getPortURI(), this.rmop_uri);
		
		this.registeredVmsRsop.put(avmUri, rsop);
		this.registeredVmsUri.add(avmURIs);
		
		this.logMessage(this.Component_URI + " : " + avmURIs + " has been added.");
		
		return rnip.getPortURI();
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#unregisterVM(final String)
	 */
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
		registeredVmsRsop.get(vmUri).doDisconnection();
	}
	
	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#unregisterVM()
	 */
	@Override
	public void unregisterVM() throws Exception {
		unregisterVM(getNextVmUriFromCursor());
	}

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmission(RequestI)
	 */
	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		this.logMessage(this.Component_URI + " : incoming request submission");
		
		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
		
		} else {
			String avmURI = getNextVmUriFromCursor();
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(avmURI); 
			
			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}
			rsop.submitRequest(r);
		}
	}

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		this.logMessage(this.Component_URI + " : incoming request submission and notification.");
		System.out.println("There's " + registeredVmsUri.size() + " registered AVMs.");
		
		if (this.registeredVmsUri.size() == 0) {
			this.logMessage(this.Component_URI + " : no registered vm.");
		
		} else {
			vmCursor = (vmCursor+1) % this.registeredVmsUri.size();
			String avmURI = getNextVmUriFromCursor();
			RequestSubmissionOutboundPort rsop = this.registeredVmsRsop.get(avmURI);
			
			this.logMessage(this.Component_URI + " is using " + avmURI);
			
			if (!rsop.connected()) {
				throw new Exception(this.Component_URI + " can't conect to vm.");
			}
			rsop.submitRequestAndNotify(r);
		}
	}
	
	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI#acceptRequestTerminationNotification(RequestI)
	 */
	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		this.logMessage("acceptRequestTerminationNotification function");
		
		RequestNotificationOutboundPort rnop = (RequestNotificationOutboundPort) this.findPortFromURI(this.rnop_uri);
		
		if (rnop == null){
			this.addRequiredInterface(RequestSubmissionI.class) ;
			rnop = new RequestNotificationOutboundPort(this.rnop_uri, this) ;
			this.addPort(rnop) ;
			rnop.publishPort() ;
			
			rnop.doConnection(
				rg_uris.get(RGPortTypes.REQUEST_NOTIFICATION_IN), 
				ClassFactory.newConnector(RequestNotificationI.class).getCanonicalName());
		}
		
		this.logMessage(this.Component_URI + " : incoming request termination notification.");
		rnop.notifyRequestTermination(r);
	}
	
	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestNotificationI#notifyRequestTermination(RequestI)
	 */
	@Override
	public void notifyRequestTermination(RequestI r) throws Exception {
		
		RequestNotificationOutboundPort rnop = (RequestNotificationOutboundPort) this.findPortFromURI(this.rnop_uri);
		this.logMessage(this.Component_URI + " : incoming request termination notification.");
		// XXX Pas utilisé.
		rnop.notifyRequestTermination(r);
	}
	
	/**
	 * @see fr.upmc.gaspardleo.componentmanagement.ShutdownableI#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		registeredVmsUri.forEach(e -> {
			String avmUri = e.get(ApplicationVMPortTypes.INTROSPECTION);
			try {
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
	
	/**
	 * Connecte l'AdmissionControler pour la réception des notifiactions
	 * @param 	AVMConnectionPort_URI	URI du port de connexion de l'application VM
	 * @param 	notificationPort_URI	URI du port pour les notifications
	 * @param 	requestMonitor_in		URI de port du RequestMonitor
	 * @throws 	Exception
	 */
	private void doAVMRequestNotificationAndMonitoringConnection(
		String AVMConnectionPort_URI,
		String notificationPort_URI,
		String requestMonitor_in) throws Exception {
		
		this.logMessage("Admission controller : connection on notification port.");
		
		if(!this.isRequiredInterface(ApplicationVMConnectionsI.class))
			this.addRequiredInterface(ApplicationVMConnectionsI.class);
		
		ApplicationVMConnectionOutboundPort avmcop = new ApplicationVMConnectionOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(avmcop);
		avmcop.publishPort();
		
		avmcop.doConnection(
			AVMConnectionPort_URI, 
			ClassFactory.newConnector(ApplicationVMConnectionsI.class).getCanonicalName());
		
		avmcop.doRequestNotificationConnection(notificationPort_URI);
		
		avmcop.doRequestMonitorConnection(requestMonitor_in);
		
		this.logMessage("Admission controller : avmcop connection status : " + avmcop.connected());
	}

	/**
	 * @see fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI#getRegisteredAVMUris()
	 */
	@Override
	public List<String> getRegisteredAVMUris() {
		return registeredVmsUri.stream()
		   .map((e) -> e.get(ApplicationVMPortTypes.INTROSPECTION))
		   .collect(Collectors.toList());
	}
	
	/**
	 * Construit les URIs du composant et de ses ports
	 * @param num_rd	Numéro du RequestDispatcher pour la création unique d'URI
	 * @return	Les URIs du composant et de ses ports
	 */
	public static HashMap<RDPortTypes, String> makeUris(int num_rd){
		HashMap<RDPortTypes, String> requestDispatcher_uris = new HashMap<RDPortTypes, String>() ;		
		requestDispatcher_uris.put(RDPortTypes.INTROSPECTION, "rd-"+num_rd);
		requestDispatcher_uris.put(RDPortTypes.REQUEST_SUBMISSION_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_SUBMISSION_OUT, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_NOTIFICATION_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_NOTIFICATION_OUT, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_DISPATCHER_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.SHUTDOWNABLE_IN, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.REQUEST_GENERATOR_MANAGER_OUT, AbstractPort.generatePortURI());
		requestDispatcher_uris.put(RDPortTypes.RQUEST_MONITOR_IN, AbstractPort.generatePortURI());
		return requestDispatcher_uris;
	}
}
