package fr.upmc.gaspardleo.applicationvm;

import java.util.HashMap;

import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesNotificationInboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.Task;
import fr.upmc.datacenter.software.applicationvm.interfaces.TaskI;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionInboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentcreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorOutboundPort;

/**
 * La classe <code> ApplicationVM </ code> implémente le composant représentant 
 * une application VM dans le centre de données.
 * 
 * <p><strong>Description</strong></p>
 * Ce composant simule l'exécution d'applications web en recevant des requêtes
 * @author Leonor & Alexandre
 */
public class ApplicationVM 
		extends fr.upmc.datacenter.software.applicationvm.ApplicationVM
		implements ApplicationVMConnectionsI, ShutdownableI {

	public static enum	ApplicationVMPortTypes {
		REQUEST_SUBMISSION,
		REQUEST_NOTIFICATION,
		MANAGEMENT,
		INTROSPECTION,
		STATIC_STATE,
		DYNAMIC_STATE,
		CONNECTION_REQUEST,
		SHUTDOWNABLE;
	}

	/** map contenant le timestamp d'arrivé de chaque reqête */
	private HashMap<RequestI, Long> requestStartTimeStamps;
	/** URI de l'application VM */
	private String vmURI;
	/** Inbound port offrant les services d'arrêt de l'application VM */
	private ShutdownableInboundPort sip;
	/** Inbound port offrent les services de connexion à l'application VM */
	private ApplicationVMConnectionInboundPort avmcip;
	/** Outbound port pôur utiliser les services du RequestMonitor */
	private RequestMonitorOutboundPort rmop;

	/**
	 * @param component_uris	URIS du composant et des ses ports
	 * @throws Exception
	 */
	public ApplicationVM(
			HashMap<ApplicationVMPortTypes, String> component_uris) throws Exception {

		super(
			component_uris.get(ApplicationVMPortTypes.INTROSPECTION), 
			component_uris.get(ApplicationVMPortTypes.MANAGEMENT), 
			component_uris.get(ApplicationVMPortTypes.REQUEST_SUBMISSION),
			component_uris.get(ApplicationVMPortTypes.REQUEST_NOTIFICATION));

		this.vmURI = component_uris.get(ApplicationVMPortTypes.INTROSPECTION);
		this.requestStartTimeStamps = new HashMap<>();
		
		this.addOfferedInterface(ApplicationVMConnectionsI.class);
		this.avmcip = new ApplicationVMConnectionInboundPort(component_uris.get(ApplicationVMPortTypes.CONNECTION_REQUEST), this);
		this.addPort(avmcip);
		this.avmcip.publishPort();
		
		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(component_uris.get(ApplicationVMPortTypes.SHUTDOWNABLE), this);
		this.addPort(this.sip);
		this.sip.publishPort();

		this.addRequiredInterface(RequestMonitorI.class);
		this.rmop = new RequestMonitorOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(this.rmop);
		this.rmop.publishPort();
		
		// VM debug
		this.toggleLogging();
		this.toggleTracing();
		
		this.logMessage("ApplicationVM made");
	}

	/**
	 * @see fr.upmc.gaspardleo.applicationvm.interfaces#doRequestNotificationConnection(String)
	 */
	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {
		
		if(!this.isRequiredInterface(RequestNotificationI.class))
			this.addRequiredInterface(RequestNotificationI.class);
		
		this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this);
		this.addPort(requestNotificationOutboundPort);
		this.requestNotificationOutboundPort.publishPort();

		try{
			this.requestNotificationOutboundPort.doConnection(
				RD_RequestNotificationInboundPortURI,
				ClassFactory.newConnector(RequestNotificationI.class).getCanonicalName());
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
		this.logMessage("AVM : rnop connection status : " + this.requestNotificationOutboundPort.connected());
	}
	
	/**
	 * @see fr.upmc.gaspardleo.applicationvm.interfaces#doRequestMonitorConnection(String)
	 */
	@Override
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception {

		this.rmop.doConnection(
			requestMonitor_in,
			ClassFactory.newConnector(RequestMonitorI.class).getCanonicalName());
	}

	/**
	 * Cée un RequestNotificationOutboundPort si il n'éxiste pas déjà et le retourne
	 * @return RequestNotificationOutboundPort
	 * @throws Exception
	 */
	public RequestNotificationOutboundPort getRequestNotificationOutboundPort() throws Exception {
		
		if (this.requestNotificationOutboundPort == null){
			
			if(!this.isRequiredInterface(RequestNotificationI.class))
				this.addRequiredInterface(RequestNotificationI.class);
			
			this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this);
		}
		return this.requestNotificationOutboundPort;
	}
	
	/**
	 * @see fr.upmc.gaspardleo.componentmanagement#startTask()
	 */
	@Override
	public void startTask() throws Exception {
		
		assert	!this.taskQueue.isEmpty() ;

		AllocatedCore ac = this.findIdleCore();
		this.logMessage("Starting task execution on core " + ((ac == null) ? "'no available core'": ac) + ".");

		if (ac != null) {
			long execTimestamp = System.currentTimeMillis();
			this.allocatedCoresIdleStatus.remove(ac) ;
			this.allocatedCoresIdleStatus.put(ac, false) ;

			TaskI t = this.taskQueue.remove() ;
			
			this.logMessage(this.vmURI + " : Request " + t.getRequest().getRequestURI() + " has been in queue for " 
					+ (execTimestamp -this.requestStartTimeStamps.get(t.getRequest())) + "ms.");
			
			rmop.addEntry(this.requestStartTimeStamps.get(t.getRequest()), execTimestamp);
				
			this.logMessage(this.vmURI + " starts request " + t.getRequest().getRequestURI()) ;
			this.runningTasks.put(t.getTaskURI(), ac);
			
			ProcessorServicesOutboundPort p = this.processorServicesPorts.get(ac.processorURI) ;
			ProcessorServicesNotificationInboundPort np = this.processorNotificationInboundPorts.get(ac.processorURI) ;
			p.executeTaskOnCoreAndNotify(t, ac.coreNo, np.getPortURI()) ;
		
		} else {
			this.logMessage("Task cancelled, couldn't find an idling core, the request is being transfered in queue.");
		}
	}

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(final RequestI r) throws Exception {
		
		if (AbstractCVM.DEBUG) 
			System.out.println("ApplicationVM>>acceptRequestSubmissionAndNotify");
		
		this.logMessage(this.vmURI + " queues request " + r.getRequestURI());
		
		Task t = new Task(r) ;
		
		this.requestStartTimeStamps.put(r, System.currentTimeMillis());
		
		this.taskQueue.add(t) ;
		this.tasksToNotify.add(t.getTaskURI()) ;
		
		this.startTask();
	}

	/**
	 * Création d'une application VM via le ComponentCreator qui gère la création dynamique
	 * @param component_URI		URI de l'application VM
	 * @param cc				ComponentCreator
	 * @return					Les URIs de l'application VM et des ses ports
	 * @throws Exception
	 */
	public static HashMap<ApplicationVMPortTypes, String> newInstance(
			String component_URI,
			ComponentCreator cc) throws Exception {
		
		HashMap<ApplicationVMPortTypes, String> component_uris = new HashMap<ApplicationVMPortTypes, String>();		
		component_uris.put(ApplicationVMPortTypes.INTROSPECTION, component_URI);
		component_uris.put(ApplicationVMPortTypes.REQUEST_SUBMISSION, AbstractPort.generatePortURI());
		component_uris.put(ApplicationVMPortTypes.MANAGEMENT, AbstractPort.generatePortURI());
		component_uris.put(ApplicationVMPortTypes.REQUEST_NOTIFICATION, AbstractPort.generatePortURI());
		component_uris.put(ApplicationVMPortTypes.SHUTDOWNABLE, AbstractPort.generatePortURI());
		component_uris.put(ApplicationVMPortTypes.CONNECTION_REQUEST, AbstractPort.generatePortURI());
		
		Object[] constructorParams = new Object[] {
				component_uris
		};
		
		try{
			cc.createComponent(ApplicationVM.class, constructorParams);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
		return component_uris;
	}
}
