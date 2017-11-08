package fr.upmc.gaspardleo.test;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.cvm.CVMComponent;

public class Test {
	private final static int NB_DATASOURCE = 10;
	
	
	private static String CVM_IPURI = AbstractPort.generatePortURI();
	private CVMComponent cvmc;
	private CVM cvm;
	private AdmissionController ac;


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
			this.cvmc 	= new CVMComponent(cvm, CVM_IPURI);

			// Admission Controller creation
			this.ac = new AdmissionController();
			
			// Simply adds some request generators to the current admission controller.
			for (int i = 0; i < NB_DATASOURCE; i++) {
				this.addDataSource(i);
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void addDataSource(int i) throws Exception {
		String rg_rgmip = AbstractPort.generatePortURI();
		String rg_rsop = AbstractPort.generatePortURI();
		String rg_rnip = AbstractPort.generatePortURI();
		String rg_rgmop = AbstractPort.generatePortURI();
		
		RequestGenerator rg;
		
		//TODO nouvelle classe extends RG avec enum et getComputerURI
		
		// Request Generator creation
		rg = new RequestGenerator(
				"rg",		// generator component URI
				500.0,		// mean time between two requests
				6000000000L,// mean number of instructions in requests
				rg_rgmip,
				rg_rsop,
				rg_rnip);

		// Rg debug
		rg.toggleTracing();
		rg.toggleLogging();


		// Components deployment
		this.cvm.deploy();
		this.cvm.deployComponent(rg);

		// Dynamic ressources creation
		String rd_rsip = this.ac.addRequestSource(
				rg_rnip,
				CVM_IPURI);

		//TODO uris inutiles
		
		// Port connections
		rg.doPortConnection(
				rg_rsop,
				rd_rsip,
				RequestSubmissionConnector.class.getCanonicalName());

		
		//TODO uris inutiles
		
		// Rg management creation
		RequestGeneratorManagementOutboundPort rgmop = new RequestGeneratorManagementOutboundPort(
				rg_rgmop,
				new AbstractComponent(0, 0) {});
		rgmop.publishPort();
		
		//TODO uris inutiles
		rgmop.doConnection(
				rg_rgmip,
				RequestGeneratorManagementConnector.class.getCanonicalName());
		
		this.rgmops.add(rgmop);

	}

	//TODO proposer un scénario qui permet de mettre en évidence le refus de requêtes
	
	public void testScenario() throws Exception {

		// start the request generation in the request generator.
		this.rgmops.forEach(rgmop -> {
			try {
				rgmop.startGeneration();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		
		// wait 20 seconds
		Thread.sleep(20000L);
		// then stop the generation.

		
		this.rgmops.forEach(rgmop -> {
			try {
				rgmop.stopGeneration();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
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
			new Thread(() -> {
				try {
					tvmc.testScenario();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).start();;

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