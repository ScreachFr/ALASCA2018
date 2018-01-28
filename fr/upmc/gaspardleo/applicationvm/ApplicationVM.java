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
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorOutboundPort;

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

	private HashMap<RequestI, Long> requestStartTimeStamps;
	
	// Misc
	private String vmURI;

	// Ports
	private ShutdownableInboundPort sip;
	private ApplicationVMConnectionInboundPort avmcip;
	private RequestMonitorOutboundPort rmop;
	
	public ApplicationVM(
			HashMap<ApplicationVMPortTypes, String> component_uris
		) throws Exception {

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

	@Override
	public void doRequestNotificationConnection(
			String RD_RequestNotificationInboundPortURI) throws Exception {
		try{
			this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this);
			this.addPort(requestNotificationOutboundPort);
			this.requestNotificationOutboundPort.publishPort();
			this.addRequiredInterface(RequestNotificationI.class);
	
			this.requestNotificationOutboundPort.doConnection(
				RD_RequestNotificationInboundPortURI,
				ClassFactory.newConnector(RequestNotificationI.class).getCanonicalName());
			
			assert this.requestNotificationOutboundPort.connected() : "rnop is not connect";
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
		this.logMessage("AVM : rnop connection status : " + this.requestNotificationOutboundPort.connected());
	}
	
	@Override
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception {

		try{
			this.rmop.doConnection(
				requestMonitor_in,
				ClassFactory.newConnector(RequestMonitorI.class).getCanonicalName());
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		System.out.println("rmop connected : " + rmop.connected());

	}

	public RequestNotificationOutboundPort getRequestNotificationOutboundPort() throws Exception {
		
		if (this.requestNotificationOutboundPort == null)
			this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this);

		return this.requestNotificationOutboundPort;
	}

	@Override
	public void startTask() throws Exception {
		
		assert	!this.taskQueue.isEmpty() ;

		AllocatedCore ac = this.findIdleCore();
		this.logMessage("Starting task execution on core " + ac + ".");

		if (ac != null) {
			long execTimestamp = System.currentTimeMillis();
			this.allocatedCoresIdleStatus.remove(ac) ;
			this.allocatedCoresIdleStatus.put(ac, false) ;

			TaskI t = this.taskQueue.remove() ;
			
			
			this.logMessage(this.vmURI + " : Request " + t.getRequest().getRequestURI() + " has been in queue for " 
					+ (execTimestamp -this.requestStartTimeStamps.get(t.getRequest())) + "ms.");
			
			try{
				rmop.addEntry(this.requestStartTimeStamps.get(t.getRequest()), execTimestamp);
			} catch (Exception e){
				e.printStackTrace();
				throw e;
			}
			this.logMessage(this.vmURI + " starts request " +
					t.getRequest().getRequestURI()) ;
			this.runningTasks.put(t.getTaskURI(), ac) ;
			ProcessorServicesOutboundPort p =
					this.processorServicesPorts.get(ac.processorURI) ;
			ProcessorServicesNotificationInboundPort np =
					this.processorNotificationInboundPorts.get(ac.processorURI) ;
			p.executeTaskOnCoreAndNotify(t, ac.coreNo, np.getPortURI()) ;
		
		} else {
			this.logMessage("Task cancelled, couldn't find an idling core.");
		}
	}
	
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
		
		/* Constructeur :
		 
				HashMap<ApplicationVMPortTypes, String> component_uris
		 */
		
		Object[] constructorParams = new Object[] {
				component_uris
		};
		
		try {
			cc.createComponent(ApplicationVM.class, constructorParams);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

		return component_uris;
	}
}
