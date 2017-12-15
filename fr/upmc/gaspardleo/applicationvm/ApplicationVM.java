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


public class ApplicationVM extends fr.upmc.datacenter.software.applicationvm.ApplicationVM
implements ApplicationVMConnectionsI {



	public static enum	ApplicationVMPortTypes {
		REQUEST_SUBMISSION, REQUEST_NOTIFICATION, MANAGEMENT, INTROSPECTION, STATIC_STATE,
		DYNAMIC_STATE
	}

	private String vmURI;

	public ApplicationVM(String vmURI, String applicationVMManagementInboundPortURI,
			String requestSubmissionInboundPortURI, String requestNotificationOutboundPortURI) throws Exception {
		super(vmURI, applicationVMManagementInboundPortURI, requestSubmissionInboundPortURI,
				requestNotificationOutboundPortURI);
		this.vmURI = vmURI;
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
			String requestNotificationOutboundPortURI,
			DynamicComponentCreator dcc) throws Exception {

		String applicationVMManagementInboundPortURI = AbstractPort.generatePortURI();
		String requestSubmissionInboundPortURI = AbstractPort.generatePortURI();

		Object[] args = new Object[] {
				vmURI, applicationVMManagementInboundPortURI,
				requestSubmissionInboundPortURI, requestNotificationOutboundPortURI
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
		return ret;

	}

}
