package fr.upmc.gaspardleo.test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.DynamicComponentCreator;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationInboundPort;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController.ACPortTypes;
import fr.upmc.gaspardleo.admissioncontroller.connectors.AdmissionControllerConnector;
import fr.upmc.gaspardleo.admissioncontroller.port.AdmissionControllerOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class Test {

	private final static int 	NB_DATASOURCE 	= 1;	
	private final static String AC_URI 			= "AC_URI";
	private final static String URI_DCC 		= "uri_dcc";
	private final static String CP_URI			= "CP_URI";

	private CVM 				cvm;
	//	private AdmissionController	ac;

	private List<RequestGeneratorManagementOutboundPort> rgmops;

	private Map<ACPortTypes, String> ac_uris;
	private Map<ComputerPoolPorts, String> cp_uris;

	public Test(){
		rgmops = new ArrayList<>();
		initTest();
	}

	private void initTest(){
		try {


			//TODO
			// CVM creation
			this.cvm 	= new CVM();
			
			DynamicComponentCreationOutboundPort dccop = new DynamicComponentCreationOutboundPort(new AbstractComponent(0, 0) {});
			dccop.publishPort();
			dccop.doConnection(AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX, 
					DynamicComponentCreationConnector.class.getCanonicalName());

			cp_uris = ComputerPool.newInstance(CP_URI, dccop);

			// Admission Controller creation
			ac_uris = AdmissionController.newInstance(AC_URI, cp_uris, dccop);

			// Simply adds some request generators to the current admission controller.
			for (int i = 0; i < NB_DATASOURCE; i++) {
				this.addDataSource(i, dccop);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	private void addDataSource(int i, DynamicComponentCreationI dcc) throws Exception {

		// Request Generator creation
		Map<RGPortTypes, String> rg  = createRequestGenerator("rg-"+i, dcc);

		// Dynamic ressources creation
		AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(AbstractPort.generatePortURI(), new AbstractComponent(0, 0) {});
		acop.publishPort();
		acop.doConnection(ac_uris.get(ACPortTypes.ADMISSION_CONTROLLER_IN), AdmissionControllerConnector.class.getCanonicalName());
		acop.addRequestDispatcher("rd-"+i, rg);
		//		this.ac.addRequestDispatcher("rd-"+i, rg);

		//this.cvm.deployComponent(rd);

		//		ArrayList<ApplicationVM> vms = this.ac.addApplicationVMs(rd);

		//		for (int j = 0; j < vms.size(); j++){
		//			this.cvm.deployComponent(vms.get(j));
		//		}

		//		ArrayList<ApplicationVMManagementOutboundPort> avmPorts = this.ac.getApplicationVMManagementOutboundPorts();
		//		
		//		for (int j = avmPorts.size() - vms.size(); j < avmPorts.size(); j++){
		//			ApplicationVMManagementOutboundPort avmPort = avmPorts.get(j);
		//			this.cvm.allocateCores(avmPort);
		//		}

		// Port connections
		//		rg.doPortConnection(
		//			rg.getRGPortsURI().get(RGPortTypes.REQUEST_SUBMISSION_OUT),
		//			rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN),
		//			RequestSubmissionConnector.class.getCanonicalName());
	}

	private Map<RGPortTypes, String> createRequestGenerator(String RG_URI, DynamicComponentCreationI dcc) throws Exception{
		Map<RGPortTypes, String> result = RequestGenerator.newInstance(RG_URI, 500.0, 6000000000L, dcc);

		createRGManagement(result.get(RGPortTypes.MANAGEMENT_IN));


		return result;
	}

	private void createRGManagement(String rg_management_in) throws Exception{

		// Rg management creation
		RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
				AbstractPort.generatePortURI(),
				new AbstractComponent(0, 0) {});

		rgmop.publishPort();

		rgmop.doConnection(
				rg_management_in,
				RequestGeneratorManagementConnector.class.getCanonicalName());

		this.rgmops.add(rgmop);
	}

	//TODO proposer un scénario qui permet de mettre en évidence le refus de requêtes

	public void testScenario() throws Exception {

		// start the request generation in the request generator.

		for(int i = 0; i < this.rgmops.size(); i++){
			RequestGeneratorManagementOutboundPort rgmop = this.rgmops.get(i);
			rgmop.startGeneration();
		}

		// wait 20 seconds
		Thread.sleep(20000L);
		// then stop the generation.

		for(int i = 0; i < this.rgmops.size(); i++){
			RequestGeneratorManagementOutboundPort rgmop = this.rgmops.get(i);
			rgmop.stopGeneration();
		}

	}

	public CVM getCvm() {
		return cvm;
	}

	public static void main(String[] args){

		// AbstractCVM.toggleDebugMode() ;
		try {
			final Test tvmc = new Test() ;
			// Deploy the components
			//			tvmc.getCvm().deploy() ;

			System.out.println("starting...") ;
			// Start them.
			tvmc.getCvm().start() ;

			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						tvmc.testScenario();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}).start();

			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			tvmc.getCvm().shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}