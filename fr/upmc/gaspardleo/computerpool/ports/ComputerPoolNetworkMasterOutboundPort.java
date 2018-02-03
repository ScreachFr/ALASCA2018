package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;

public class ComputerPoolNetworkMasterOutboundPort 
	extends AbstractOutboundPort 
	implements ComputerPoolNetworkMasterI {


	private static final long serialVersionUID = -6041921663130597612L;

	public ComputerPoolNetworkMasterOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolNetworkMasterI.class, owner);
	}
	
	@Override
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) throws Exception {
		((ComputerPoolNetworkMasterI)this.connector).registerComputerPool(computerPoolUri, compterPoolInboundPortUri);
		
	}

	@Override
	public void unregisterComputerPool(String computerPoolUri) throws Exception {
		((ComputerPoolNetworkMasterI)this.connector).unregisterComputerPool(computerPoolUri);
	}

	@Override
	public HashMap<String, String> getAvailableComputerPools() throws Exception {
		return ((ComputerPoolNetworkMasterI)this.connector).getAvailableComputerPools();
	}

}
