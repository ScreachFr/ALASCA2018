package fr.upmc.gaspardleo.cvm;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

/**
 * La classe <code> CVM </ code> définit le comportement de la CVM non distribuée
 * pour le déploiement et l'allocation de coeurs.
 * @author Leonor & Alexandre
 */
public class CVM extends AbstractCVM {
	
	/** Liste des coeurs alloués */
	private AllocatedCore[] cores;
	/** Coeur courant */
	private int currentCore;
	
	public CVM() throws Exception {		
		super();
				
		this.currentCore = 0;
	}

	/**
	 * @see fr.upmc.components.cvm#deploy()
	 */
	@Override
	public void deploy() throws Exception {
		
		AbstractComponent.configureLogging("", "", 0, '|');
		Processor.DEBUG = true;
		super.deploy();
	}
	
	/**
	 * @see fr.upmc.components.cvm#start()
	 */
	@Override
	public void start() throws Exception {
		
		super.start();
	}

	/**
	 * Récupère les coeurs alloués
	 * @return	Les coeurs alloués
	 */
	private AllocatedCore[] getAllocatedCore() {
		
		AllocatedCore[] result = new AllocatedCore[1];
		
		result[0] = this.cores[this.currentCore];
		
		this.currentCore = (this.currentCore + 1) % this.cores.length;
				
		return result;
	}
	
	/**
	 * Alloue un coeur à une application VM via son outbound port
	 * @param 	avmPort		Outbound port de l'application VM
	 * @throws 	Exception
	 */
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		
		avmPort.allocateCores(getAllocatedCore()) ;
	}
}
