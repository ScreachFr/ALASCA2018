package fr.upmc.gaspardleo.applicationvm;

import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
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
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableInboundPort;


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

	// Misc
	private String vmURI;

	// Ports
	private ShutdownableInboundPort sip;
	private ApplicationVMConnectionInboundPort avmcip;
	
	public ApplicationVM(String vmURI,
			String applicationVMManagementInboundPortURI,
			String requestSubmissionInboundPortURI,
			String requestNotificationOutboundPortURI,
			String applicationVMConnectionPort_URI,
			String shutdownableInboundPort_URI) throws Exception {
		
		super(vmURI, 
				applicationVMManagementInboundPortURI,
				requestSubmissionInboundPortURI,
				requestNotificationOutboundPortURI);
		this.vmURI = vmURI;
		
		
		this.addOfferedInterface(ApplicationVMConnectionsI.class);
		this.avmcip = new ApplicationVMConnectionInboundPort(applicationVMConnectionPort_URI, this);
		this.addPort(avmcip);
		this.avmcip.publishPort();
		
		this.addOfferedInterface(ShutdownableI.class);
		this.sip = new ShutdownableInboundPort(shutdownableInboundPort_URI, this);
		this.addPort(this.sip);
		this.sip.publishPort();
		
		this.toggleLogging();
		
		
	}

	@Override
	public void doRequestNotificationConnection(String RD_RequestNotificationInboundPortURI) throws Exception {
		this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this);
		this.addPort(requestNotificationOutboundPort);
		this.requestNotificationOutboundPort.publishPort();
		this.addRequiredInterface(RequestNotificationI.class);

		this.requestNotificationOutboundPort.doConnection(RD_RequestNotificationInboundPortURI, RequestNotificationConnector.class.getCanonicalName());
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

	public Map<ApplicationVMPortTypes, String>	getNewAVMPortsURI() throws Exception {
		HashMap<ApplicationVMPortTypes, String> ret =
				new HashMap<ApplicationVMPortTypes, String>();		
		ret.put(ApplicationVMPortTypes.REQUEST_SUBMISSION,
				this.requestSubmissionInboundPort.getPortURI());
		ret.put(ApplicationVMPortTypes.MANAGEMENT,
				this.applicationVMManagementInboundPort.getPortURI());
		ret.put(ApplicationVMPortTypes.INTROSPECTION,
				this.vmURI);
		ret.put(ApplicationVMPortTypes.REQUEST_NOTIFICATION, 
				this.requestNotificationOutboundPort.getPortURI());
		return ret;
	}


	public static Map<ApplicationVMPortTypes, String> newInstance(
			String vmURI,
			DynamicComponentCreator dcc) throws Exception {

		String applicationVMManagementInboundPortURI = AbstractPort.generatePortURI();
		String requestSubmissionInboundPortURI = AbstractPort.generatePortURI();
		String requestNotificationOutboundPortURI = AbstractPort.generatePortURI();
		String shutdownableInboundPort = AbstractPort.generatePortURI();
		
		Object[] args = new Object[] {
				vmURI,
				applicationVMManagementInboundPortURI,
				requestSubmissionInboundPortURI,
				requestNotificationOutboundPortURI,
				shutdownableInboundPort
		};

		dcc.createComponent(ApplicationVM.class.getCanonicalName(), args);
		
		HashMap<ApplicationVMPortTypes, String> ret =
				new HashMap<ApplicationVMPortTypes, String>();		
		ret.put(ApplicationVMPortTypes.REQUEST_SUBMISSION,
				requestSubmissionInboundPortURI);
		ret.put(ApplicationVMPortTypes.MANAGEMENT,
				applicationVMManagementInboundPortURI);
		ret.put(ApplicationVMPortTypes.INTROSPECTION,
				vmURI);
		ret.put(ApplicationVMPortTypes.REQUEST_NOTIFICATION, 
				requestNotificationOutboundPortURI);
		ret.put(ApplicationVMPortTypes.SHUTDOWNABLE, shutdownableInboundPort);
		
		return ret;

	}

}
