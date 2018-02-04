package fr.upmc.gaspardleo.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.componentmanagement.ShutdownableI;
import fr.upmc.gaspardleo.componentmanagement.ports.ShutdownableOutboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator.PerformanceRegulatorPorts;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator.RegulationStrategies;
import fr.upmc.gaspardleo.performanceregulator.data.TargetValue;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.ports.PerformanceRegulatorOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;
import fr.upmc.gaspardleo.requestgenerator.interfaces.RequestGeneratorConnectionI;
import fr.upmc.gaspardleo.requestgenerator.ports.RequestGeneratorOutboundPort;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor.RequestMonitorPorts;
import fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerInboundPort;
import fr.upmc.gaspardleo.classfactory.ClassFactory;

/**
 * La classe <code> AdmissionController </ code> implémente le composant représentant 
 * un contrôlleur d'admission dans le centre de données.
 * 
 * <p><strong>Description</strong></p>
 * Ce composant gère la création et la suppression de ressources pour le traitement des requêtes.
 * 
 * @author Leonor & Alexandre
 */
public 	class 		AdmissionController 
		extends 	AbstractComponent
		implements 	AdmissionControllerI {

	public static enum	ACPortTypes {
		ADMISSION_CONTROLLER_IN
	}
	
	/** Inbound port offrant les services de l'admission contrôlleur */
	private AdmissionControllerInboundPort acip;
	/**	Liste d'outbound port utilisé pour manager les applications VM */
	private ArrayList<ApplicationVMManagementOutboundPort> avmPorts;
	/** Liste d'URIs pour gérer les duos RequestGenerator - RequestDispatcher */
	private HashMap<HashMap<RGPortTypes, String>, HashMap<RDPortTypes, String>> requestSources;
	/** Liste d'URIs pour gérer le ComputerPool */
	private HashMap<ComputerPoolPorts, String> computerPoolURIs;
	
	/**
	 * @param 	computerPoolUri		URI du composant ComputerPool
	 * @param 	ac_uris				URI du composant en lui même
	 * @throws 	Exception
	 */
	public AdmissionController(
		HashMap<ComputerPoolPorts, String> computerPoolUri,
		HashMap<ACPortTypes, String> ac_uris) throws Exception{		
		
		super(1, 1);

		this.requestSources = new HashMap<>();

		this.avmPorts 	= new ArrayList<ApplicationVMManagementOutboundPort>();
		this.computerPoolURIs = computerPoolUri;

		this.addOfferedInterface(AdmissionControllerI.class);
		this.acip = new AdmissionControllerInboundPort(ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), this);
		this.addPort(this.acip);
		this.acip.publishPort();

		this.toggleLogging();
		this.toggleTracing();		
		this.logMessage("AdmissionController made");
	}
	
	public AdmissionController() {
		super(1,1);
	}

	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI#addRequestSource(HashMap<RDPortTypes, String>, HashMap<RGPortTypes, String>, String)
	 */
	@Override
	public void addRequestSource(
			Integer howManyAVMsOnStartup,
			HashMap<RDPortTypes, String> RD_uris,
			HashMap<RGPortTypes, String> RG_uris,
			String rg_monitor_in) throws Exception {
		
		this.logMessage("Admission controller : adding a request source...");

		String rd_URI = RD_uris.get(RDPortTypes.INTROSPECTION);
		
		//Creation of the RequestMonitor
		HashMap<RequestMonitorPorts, String> rm_uris = RequestMonitor.makeUris(rg_monitor_in, rd_URI);
		RequestMonitor rm = new RequestMonitor(rm_uris, 0.5);
		rm.start();
		
		//Request Generator port
		this.addRequiredInterface(RequestGeneratorConnectionI.class);
		RequestGeneratorOutboundPort rgop = new RequestGeneratorOutboundPort(this);
		this.addPort(rgop);
		rgop.publishPort();
	
		rgop.doConnection(
			RG_uris.get(RGPortTypes.CONNECTION_IN), 
			ClassFactory.newConnector(RequestGeneratorConnectionI.class).getCanonicalName());
		
		//Connection with the RequestDispatcher
		rgop.doConnectionWithRD(
			RD_uris.get(RDPortTypes.REQUEST_SUBMISSION_IN));	
		
		// Performance regulator creation
		HashMap<PerformanceRegulatorPorts, String> performanceRegulator_uris = PerformanceRegulator.makeUris(rd_URI);
		PerformanceRegulator pr = new PerformanceRegulator(
			performanceRegulator_uris, 
			RD_uris, rm_uris, 
			computerPoolURIs,
			RegulationStrategies.SIMPLE_AVM,
			new TargetValue(2000.0, 0.0));	
		pr.start();
		
		//PerformanceRegulator port
		if(!this.isRequiredInterface(PerformanceRegulatorI.class))
			this.addRequiredInterface(PerformanceRegulatorI.class);
		
		PerformanceRegulatorOutboundPort prop = new PerformanceRegulatorOutboundPort(this);
		this.addPort(prop);
		prop.publishPort();
		
		prop.doConnection(
			performanceRegulator_uris.get(PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN),
			ClassFactory.newConnector(PerformanceRegulatorI.class).getCanonicalName());
		
		//Addition of AVM to the RD
		for (int i = 0; i < howManyAVMsOnStartup; i++) {
			if (!prop.addAVMToRD())
				this.logMessage("Admission controller : not any avms available at the moment.");
		}
			
		this.logMessage("Admission controller : Request source successfully added!");
	}
	
	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interface.AdmissionControllerI#removeRequestSource(String)
	 */
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
		
		if(!this.isRequiredInterface(ShutdownableI.class));
			this.addRequiredInterface(ShutdownableI.class);
			
		ShutdownableOutboundPort sop = new ShutdownableOutboundPort(this);
		this.addPort(sop);
		sop.publishPort();

		sop.shutdown();

		requestSources.remove(optRD.get());

		// TODO Attendre la fin du shutdown avant de faire ça ?
		//sop.doDisconnection();
	}

	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI#getApplicationVMManagementOutboundPorts()
	 */
	@Override
	public ArrayList<ApplicationVMManagementOutboundPort> getApplicationVMManagementOutboundPorts() {
		return this.avmPorts;
	}
	
	/**
	 * Construit les URIs pour le composant et ses ports.
	 * @param 	introspection_uri		URI du composant en lui même.
	 * @return							Les URIs pour le composant et ses ports.
	 */
	public static HashMap<ACPortTypes, String> makeUris(String introspection_uri){
		HashMap<ACPortTypes, String> ac_uris = new HashMap<ACPortTypes, String>();		
		ac_uris.put(ACPortTypes.ADMISSION_CONTROLLER_IN, introspection_uri);
		return ac_uris;
	}

	/**
	 * @see fr.upmc.gaspardleo.admissioncontroller.interfaces.AdmissionControllerI#createNewRequestDispatcher(int, HashMap<RGPortTypes, String>, HashMap<ACPortTypes, String>)
	 */
	@Override
	public void createNewRequestDispatcher(
			Integer num_rd,
			HashMap<RGPortTypes, String> rg_uris, 
			HashMap<ACPortTypes, String> ac_uris) throws Exception {
		RequestDispatcher rd = new RequestDispatcher(RequestDispatcher.makeUris(num_rd), rg_uris, ac_uris);
		rd.start();
	}
}