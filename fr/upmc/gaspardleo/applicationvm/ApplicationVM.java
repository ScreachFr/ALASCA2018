package fr.upmc.gaspardleo.applicationvm;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesNotificationInboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.interfaces.TaskI;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.gaspardleo.applicationvm.interfaces.ApplicationVMConnectionsI;
import fr.upmc.gaspardleo.applicationvm.ports.ApplicationVMConnectionInboundPort;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;

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

	// Ports
	private ShutdownableInboundPort sip;
	private ApplicationVMConnectionInboundPort avmcip;

	public ApplicationVM(
			String component_URI, 
			String applicationVMManagement_In,
			String requestSubmission_In, 
			String requestNotification_Out,
			String applicationVMConnectionPort_URI,
			String shutdownableInboundPort_URI) throws Exception {

		super(
			component_URI, 
			applicationVMManagement_In, 
			requestSubmission_In,
			requestNotification_Out);

		this.addOfferedInterface(ApplicationVMConnectionsI.class);
		this.avmcip = new ApplicationVMConnectionInboundPort(applicationVMConnectionPort_URI, this);
		this.addPort(avmcip);
		this.avmcip.publishPort();

		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(shutdownableInboundPort_URI, this);
		this.addPort(this.sip);
		this.sip.publishPort();
		
		// VM debug
		this.toggleLogging();
	}

	@Override
	public void doRequestNotificationConnection(
			String RD_RequestNotificationInboundPortURI) throws Exception {
		
		this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this);
		this.addPort(requestNotificationOutboundPort);
		this.requestNotificationOutboundPort.publishPort();
		this.addRequiredInterface(RequestNotificationI.class);

		this.requestNotificationOutboundPort
				.doConnection(RD_RequestNotificationInboundPortURI,
						RequestNotificationConnector.class.getCanonicalName());
		
		this.logMessage("AVM : rnop connection status : " + this.requestNotificationOutboundPort.connected());
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
			this.allocatedCoresIdleStatus.remove(ac) ;
			this.allocatedCoresIdleStatus.put(ac, false) ;
			TaskI t = this.taskQueue.remove() ;
			this.logMessage(this.vmURI + " starts request " +
					t.getRequest().getRequestURI()) ;
			this.runningTasks.put(t.getTaskURI(), ac) ;
			ProcessorServicesOutboundPort p =
					this.processorServicesPorts.get(ac.processorURI) ;
			ProcessorServicesNotificationInboundPort np =
					this.processorNotificationInboundPorts.get(ac.processorURI) ;
			p.executeTaskOnCoreAndNotify(t, ac.coreNo, np.getPortURI()) ;
		
		} else {
			// TODO
			this.logMessage("Task cancelled, couldn't find an idling core.");
		}
	}

	public static Map<ApplicationVMPortTypes, String> newInstance(
			String component_URI,
			ComponentCreator cc) throws Exception {

		String applicationVMManagement_In = AbstractPort.generatePortURI();
		String requestSubmission_In = AbstractPort.generatePortURI();
		String requestNotification_Out = AbstractPort.generatePortURI();
		String applicationVMConnectionPort_URI = AbstractPort.generatePortURI();
		String shutdownableInboundPort = AbstractPort.generatePortURI();
		
		Object[] constructorParams = new Object[] {
				component_URI,
				applicationVMManagement_In,
				requestSubmission_In, 
				requestNotification_Out,
				applicationVMConnectionPort_URI,
				shutdownableInboundPort
		};
		
		System.out.println("AVM inst call...");
		
		try {
			cc.createComponent(ApplicationVM.class, constructorParams);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		System.out.println("AVM inst done.");
		
		HashMap<ApplicationVMPortTypes, String> ret = new HashMap<ApplicationVMPortTypes, String>();		
		ret.put(ApplicationVMPortTypes.REQUEST_SUBMISSION, requestSubmission_In);
		ret.put(ApplicationVMPortTypes.MANAGEMENT, applicationVMManagement_In);
		ret.put(ApplicationVMPortTypes.INTROSPECTION, component_URI);
		ret.put(ApplicationVMPortTypes.REQUEST_NOTIFICATION, requestNotification_Out);
		ret.put(ApplicationVMPortTypes.SHUTDOWNABLE, shutdownableInboundPort);
		ret.put(ApplicationVMPortTypes.CONNECTION_REQUEST, applicationVMConnectionPort_URI);

		return ret;
	}
}
