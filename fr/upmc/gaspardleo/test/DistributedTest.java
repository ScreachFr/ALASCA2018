package fr.upmc.gaspardleo.test;

import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.gaspardleo.admissioncontroller.AdmissionController;
import fr.upmc.gaspardleo.cvm.CVM;
import fr.upmc.gaspardleo.cvm.CVMComponent;
import fr.upmc.gaspardleo.cvm.CVMComponent.CVMPortTypes;

public class DistributedTest 
	extends	AbstractDistributedCVM{
	
	private static final String Datacenter 			= "datacenter";
	private static final String Datacenterclient 	= "datacenterclient";
	
	public DistributedTest(String[] args) throws Exception {
		super(args);
	}
	
	private CVMComponent 		cvmc;
	private CVM 				cvm;
	private AdmissionController	ac;
	
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
			this.cvm = new CVM();
			this.cvmc = new CVMComponent(cvm);
			this.cvm.deploy();
			
			this.ac = new AdmissionController(
					this.cvmc.getCVMPortsURI().get(CVMPortTypes.CVM_IN));
			
		} else {
			if (thisJVMURI.equals(Datacenterclient)){
				
			
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.instantiateAndPublish();
	}
	
	@Override
	public void interconnect() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
			
			
		} else {
			if (thisJVMURI.equals(Datacenterclient)){
				
				
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.interconnect();
	}
	
	public void start() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
			
			
		} else {
			if (thisJVMURI.equals(Datacenterclient)){
				
			} else {
				throw new RuntimeException("unknown JVM " + thisJVMURI);
			}
		}
		super.start();
	}
	
	
	public static void main(String[] args){
		

	}
}