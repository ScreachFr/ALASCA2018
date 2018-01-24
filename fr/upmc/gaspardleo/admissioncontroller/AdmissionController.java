package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.gaspardleo.componentCreator.ComponentCreator;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.connectors.ComputerPoolConnector;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator.PerformanceRegulatorPorts;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator.RegulationStrategies;
import fr.upmc.gaspardleo.performanceregulator.connectors.PerformanceRegulatorConnector;
import fr.upmc.gaspardleo.performanceregulator.data.TargetValue;
import fr.upmc.gaspardleo.performanceregulator.ports.PerformanceRegulatorOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
import fr.upmc.gaspardleo.requestgenerator.connectors.RequestGeneraterConnector;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorOutboundPort;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor.RequestMonitorPorts;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerInboundPort;

public class AdmissionController 
		extends AbstractComponent
		implements AdmissionControllerI{

	public static enum	ACPortTypes {
		ADMISSION_CONTROLLER_IN;
	}
	
	private AdmissionControllerInboundPort acip;
	private ArrayList<ApplicationVMManagementOutboundPort> avmPorts;
	// Map<RequestGenerator, RequestDispatcher>
	private HashMap<HashMap<RGPortTypes, String>, HashMap<RDPortTypes, String>> requestSources;
	private HashMap<ComputerPoolPorts, String> computerPoolURIs;
	private ComputerPoolOutboundPort cpop;
	private ComponentCreator cc;
	
	public AdmissionController(
			HashMap<ComputerPoolPorts, String> computerPoolUri,
			HashMap<ACPortTypes, String> ac_uris,
			ComponentCreator cc
			) throws Exception{		
		
		super(1, 1);

		this.requestSources = new HashMap<>();

		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		this.computerPoolURIs = computerPoolUri;

		this.addOfferedInterface(AdmissionControllerI.class);
		this.acip = new AdmissionControllerInboundPort(ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), this);
		this.addPort(this.acip);
		this.acip.publishPort();
		
		if(AbstractCVM.isDistributed){
			assert this.acip.isDistributedlyPublished() : "ADMISSION_CONTROLLER_IN isn't distributedly published";
			System.out.println("[DEBUG LEO] AdmissionControllerInboundPort uri : " + ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN));
			System.out.println("[DEBUG LEO] AdmissionControllerInboundPort is distributedly published ? -> " + this.acip.isDistributedlyPublished());
		}
		
		this.addRequiredInterface(ComputerPoolI.class);
		this.cpop = new ComputerPoolOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(cpop);
		this.cpop.publishPort();

//		this.cpop.doConnection(
//				computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
//				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());		
		
		this.cpop.doConnection(
			computerPoolURIs.get(ComputerPoolPorts.COMPUTER_POOL), 
			ComputerPoolConnector.class.getCanonicalName());		

		
		this.cc = cc;
		
		this.toggleLogging();
		this.toggleTracing();		
		this.logMessage("AdmissionController made");
	}

	@Override
	public void addRequestDispatcher(
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris,
			String rg_monitor_in) throws Exception {
		
		this.logMessage("Admission controller : adding a request source...");

		String rd_URI = RD_uris.get(RDPortTypes.INTROSPECTION);
		
		HashMap<RequestMonitorPorts, String> requestMonitorURIs;
		
		HashMap<RequestMonitorPorts, String> rm_uris = new HashMap<>();
		rm_uris.put(RequestMonitorPorts.REQUEST_MONITOR_IN, rg_monitor_in);		
		rm_uris.put(RequestMonitorPorts.INTROSPECTION, "rm-" + rd_URI);
		
		RequestMonitor rm = new RequestMonitor(rm_uris, 0.5);
		
		//Request Generator port
		this.addRequiredInterface(RequestGeneratorConnectionI.class);
		RequestGeneratorOutboundPort rgop = new RequestGeneratorOutboundPort(this);
		this.addPort(rgop);
		rgop.publishPort();
		
//		rgop.doConnection(
//				RG_uris.get(RGPortTypes.CONNECTION_IN), 
//				ClassFactory.newConnector(RequestGeneratorConnectionI.class).getCanonicalName());
		
		rgop.doConnection(
				RG_uris.get(RGPortTypes.CONNECTION_IN), 
				RequestGeneraterConnector.class.getCanonicalName());

		rgop.doConnectionWithRD(
				RD_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN));	
				
		// Performance regulator creation

		HashMap<PerformanceRegulatorPorts, String> performanceRegulator_uris = new HashMap<>();
		performanceRegulator_uris.put(PerformanceRegulatorPorts.INTROSPECTION, rd_URI + "-pr");
		performanceRegulator_uris.put(PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN, AbstractPort.generatePortURI());
		
		PerformanceRegulator pr = new PerformanceRegulator(
				performanceRegulator_uris, 
				RD_uris, rm_uris, 
				computerPoolURIs,
				RegulationStrategies.SIMPLE_AVM,
				new TargetValue(2000.0, 0.0));
		
		PerformanceRegulatorOutboundPort prop = new PerformanceRegulatorOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(prop);
		prop.publishPort();
		
//		prop.doConnection(pr_uris.get(PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN),
//				ClassFactory.newConnector(PerformanceRegulatorI.class).getCanonicalName());
		
		prop.doConnection(performanceRegulator_uris.get(
				PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN),
				PerformanceRegulatorConnector.class.getCanonicalName());
		
		prop.addAVMToRD();

		this.logMessage("Admission controller : Request source successfully added!");
	}
	
	@Override
	public void removeRequestSource(String requestGeneratorURI) throws Exception {

		Optional<HashMap<RGPortTypes,String>> optRD = 
				requestSources.keySet().stream()
				.filter((e) -> e.get(RGPortTypes.INTROSPECTION).equals(requestGeneratorURI))
				.findFirst();

		if (!optRD.isPresent()) {
			this.logMessage("Remove request source : Can't find the request generator you're looking for!");
			return;
		}

		ShutdownableOutboundPort sop = new ShutdownableOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(sop);
		sop.publishPort();

		sop.shutdown();

		requestSources.remove(optRD.get());

		// TODO Attendre la fin du shutdown avant de faire ça ?
		//sop.doDisconnection();
	}

	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {

		return this.avmPorts;
	}
	
//	public static HashMap<ACPortTypes, String> newInstance(
//			HashMap<ComputerPoolPorts, String> computerPoolUri,
//			HashMap<ACPortTypes, String> ac_uris,
//			ComponentCreator cc) throws Exception{
//		
//		/*Constructeur :
//		 
//		  	HashMap<ComputerPoolPorts, String> computerPoolUri,
//			HashMap<ACPortTypes, String> ac_uris,
//			ComponentCreator cc
//		 */
//		
//		Object[] constructorParams = new Object[] {
//				computerPoolUri,
//				ac_uris,
//				cc
//		};
//
//		try {
//			cc.createComponent(AdmissionController.class, constructorParams);
//		} catch(Exception e) {
//			e.printStackTrace();
//			throw e;
//		}
//
//		return ac_uris;		
//	}
}