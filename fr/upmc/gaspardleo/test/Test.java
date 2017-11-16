package fr.upmc.gaspardleo.test;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.cvm.CVMComponent;
import fr.upmc.gaspardleo.cvm.CVMComponent.CVMPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator;
import fr.upmc.gaspardleo.requestgenerator.RequestGenerator.RGPortTypes;

public class Test {
	
	private final static int 	NB_DATASOURCE = 10;
	
	private CVMComponent 		cvmc;
	private CVM 				cvm;
	private AdmissionController	ac;

	private List<RequestGeneratorManagementOutboundPort> rgmops;

	public Test(){
		rgmops = new ArrayList<>();
		initTest();
	}

	private void initTest(){
		try {	

			// CVM creation
			this.cvm 	= new CVM();

			// CVM Component creation
			this.cvmc 	= new CVMComponent(cvm);

			// Admission Controller creation
			this.ac = new AdmissionController(
				this.cvmc.getCVMPortsURI().get(CVMPortTypes.INTROSPECTION));
			
//			AdmissionControllerOutboundPort acop = new AdmissionControllerOutboundPort(
//					this.ac.getACPortsURI().get(ACPortTypes.INTROSECTION),
//					new AbstractComponent(0, 0) {});
//			acop.publishPort();
//			acop.doConnection(
//					this.cvmc.getCVMPortsURI().get(CVMPortTypes.INTROSPECTION),
//					AdmissionControllerConnector.class.getCanonicalName());
			
			// Simply adds some request generators to the current admission controller.
			for (int i = 0; i < NB_DATASOURCE; i++) {
				this.addDataSource(i);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void addDataSource(int i) throws Exception {
						
		// Request Generator creation
		RequestGenerator rg  = createRequestGenerator("rg-"+i);

		// Dynamic ressources creation
		RequestDispatcher rd = this.ac.addRequestDispatcher(
			"rd-"+i,
			rg.getRGPortsURI().get(RGPortTypes.REQUEST_NOTIFICATION_IN));
		
		this.cvm.deployComponent(rd);
		
		ArrayList<ApplicationVM> vms = this.ac.addApplicationVMs(rd);
		
		for (int j = 0; j < vms.size(); j++){
			this.cvm.deployComponent(vms.get(j));
		}
		
		// Port connections
		rg.doPortConnection(
			rg.getRGPortsURI().get(RGPortTypes.REQUEST_SUBMISSION_OUT),
			rd.getRDPortsURI().get(RDPortTypes.REQUEST_SUBMISSION_IN),
			RequestSubmissionConnector.class.getCanonicalName());
	}
	
	private RequestGenerator createRequestGenerator(String RG_URI) throws Exception{
		
		// Request Generator creation
		RequestGenerator rg  = new RequestGenerator(RG_URI);

		// Rg debug
		rg.toggleTracing();
		rg.toggleLogging();

		// Components deployment
		this.cvm.deploy();
		this.cvm.deployComponent(rg);
		
		createRGManagement(rg);
		
		return rg;
	}
	
	private void createRGManagement(RequestGenerator rg) throws Exception{
		
		// Rg management creation
		RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
			AbstractPort.generatePortURI(),
			new AbstractComponent(0, 0) {});

		rgmop.publishPort();
		
		rgmop.doConnection(
			rg.getRGPortsURI().get(RGPortTypes.MANAGEMENT_IN),
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
	
	public CVMComponent getCvmc() {
		return cvmc;
	}
	
	public static void main(String[] args){

		// AbstractCVM.toggleDebugMode() ;
		try {
			final Test tvmc = new Test() ;
			// Deploy the components
			tvmc.getCvm().deploy() ;

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