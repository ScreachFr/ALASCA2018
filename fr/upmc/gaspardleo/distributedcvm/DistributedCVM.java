package fr.upmc.gaspardleo.distributedcvm;

import fr.upmc.components.cvm.AbstractDistributedCVM;

public class DistributedCVM 
	extends	AbstractDistributedCVM{
	
	private static final String Datacenter 			= "datacenter";
	private static final String Datacenterclient 	= "datacenterclient";
	
	public DistributedCVM(String[] args) throws Exception {
		super(args);
	}
	
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if(thisJVMURI.equals(Datacenter)){
			
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