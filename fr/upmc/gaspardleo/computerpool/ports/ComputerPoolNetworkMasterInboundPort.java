package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.computerpool.ComputerPoolNetworkMaster;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;

public class ComputerPoolNetworkMasterInboundPort 
	extends AbstractInboundPort 
	implements ComputerPoolNetworkMasterI {

	public ComputerPoolNetworkMasterInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolNetworkMasterI.class, owner);
	}

	private static final long serialVersionUID = 7303969117519739268L;

	@Override
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) throws Exception {
		final ComputerPoolNetworkMaster cpnm = (ComputerPoolNetworkMaster)this.owner;
		cpnm.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPoolNetworkMaster>(){
					@Override
					public ComputerPoolNetworkMaster call() throws Exception {
						cpnm.registerComputerPool(computerPoolUri, compterPoolInboundPortUri);
						return cpnm;
					}});
	}

	@Override
	public void unregisterComputerPool(String computerPoolUri) throws Exception {
		final ComputerPoolNetworkMaster cpnm = (ComputerPoolNetworkMaster)this.owner;
		cpnm.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPoolNetworkMaster>(){
					@Override
					public ComputerPoolNetworkMaster call() throws Exception {
						cpnm.unregisterComputerPool(computerPoolUri);
						return cpnm;
					}});
	}

	@Override
	public HashMap<String, String> getAvailableComputerPools() throws Exception {
		final ComputerPoolNetworkMaster cpnm = (ComputerPoolNetworkMaster)this.owner;
		return cpnm.handleRequestSync(
				new ComponentI.ComponentService<HashMap<String, String>>(){
					@Override
					public HashMap<String, String> call() throws Exception {
						return cpnm.getAvailableComputerPools();
					}});
	}

}
