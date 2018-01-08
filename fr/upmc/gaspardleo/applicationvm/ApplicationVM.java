package fr.upmc.gaspardleo.applicationvm;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesNotificationInboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.Task;
import fr.upmc.datacenter.software.applicationvm.interfaces.TaskI;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionInboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorOutboundPort;

public class ApplicationVM extends fr.upmc.datacenter.software.applicationvm.ApplicationVM
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

	private Map<RequestI, Long> requestStartTimeStamps;
	
	// Misc
	private String vmURI;

	// Ports
	private ShutdownableInboundPort sip;
	private ApplicationVMConnectionInboundPort avmcip;
	private RequestMonitorOutboundPort rmop;
	
	public ApplicationVM(
			String component_URI, 
			String applicationVMManagement_In,
			String requestSubmission_In, 
			String requestNotification_Out,
			String applicationVMConnectionPort_URI,
			String shutdownableInboundPort_URI
		) throws Exception {

		super(
				component_URI, 
				applicationVMManagement_In, 
				requestSubmission_In,
				requestNotification_Out);

		this.vmURI = component_URI;
		this.requestStartTimeStamps = new HashMap<>();
		
		this.addOfferedInterface(ApplicationVMConnectionsI.class);
		this.avmcip = new ApplicationVMConnectionInboundPort(applicationVMConnectionPort_URI, this);
		this.addPort(avmcip);
		this.avmcip.publishPort();

		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(shutdownableInboundPort_URI, this);
		this.addPort(this.sip);
		this.sip.publishPort();

		this.addRequiredInterface(RequestMonitorI.class);
		this.rmop = new RequestMonitorOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(this.rmop);
		this.rmop.publishPort();
		
		// VM debug
		this.toggleLogging();
		this.toggleTracing();
	}

	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {

		this.requestNotificationOutboundPort
		.doConnection(RD_RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());

		this.logMessage("AVM : rnop connected to " + RD_RequestNotificationInboundPortURI);
		this.logMessage("AVM : rnop connection status : " + this.requestNotificationOutboundPort.connected());
	}
	
	@Override
	public void doRequestMonitorConnection(String requestMonitor_in) throws Exception {
		this.rmop.doConnection(requestMonitor_in,
				ClassFactory.newConnector(RequestMonitorI.class).getCanonicalName());
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
			rmop.addEntry(this.requestStartTimeStamps.get(t.getRequest()), execTimestamp);
			
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
			DynamicComponentCreationOutboundPort dcc,
			String component_URI) throws Exception {

		String applicationVMManagement_In = AbstractPort.generatePortURI();
		String requestSubmission_In = AbstractPort.generatePortURI();
		String requestNotification_Out = AbstractPort.generatePortURI();
		String applicationVMConnectionPort_URI = AbstractPort.generatePortURI();
		String shutdownableInboundPort = AbstractPort.generatePortURI();


		Object[] args = new Object[] {
				component_URI,
				applicationVMManagement_In,
				requestSubmission_In, 
				requestNotification_Out,
				applicationVMConnectionPort_URI,
				shutdownableInboundPort
		};

		try {
			dcc.createComponent(ApplicationVM.class.getCanonicalName(), args);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}

		HashMap<ApplicationVMPortTypes, String> ret =
				new HashMap<ApplicationVMPortTypes, String>();		
		ret.put(ApplicationVMPortTypes.REQUEST_SUBMISSION, requestSubmission_In);
		ret.put(ApplicationVMPortTypes.MANAGEMENT, applicationVMManagement_In);
		ret.put(ApplicationVMPortTypes.INTROSPECTION, component_URI);
		ret.put(ApplicationVMPortTypes.REQUEST_NOTIFICATION, requestNotification_Out);
		ret.put(ApplicationVMPortTypes.SHUTDOWNABLE, shutdownableInboundPort);
		ret.put(ApplicationVMPortTypes.CONNECTION_REQUEST, applicationVMConnectionPort_URI);

		return ret;

	}

}
