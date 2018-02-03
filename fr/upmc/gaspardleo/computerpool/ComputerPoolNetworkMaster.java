package fr.upmc.gaspardleo.computerpool;

import java.util.HashMap;

import fr.upmc.components.AbstractComponent;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolNetworkMasterInboundPort;

public class ComputerPoolNetworkMaster 
	extends AbstractComponent
	implements ComputerPoolNetworkMasterI{
	
	private String uri;
	
	private HashMap<String, String> pools;
	
	
	private ComputerPoolNetworkMasterInboundPort cpnmip;
	
	public ComputerPoolNetworkMaster(String componentUri, String computerPoolInboundPortUri) throws Exception {
		super(1, 1);
		
		this.uri = componentUri;
		
		this.pools = new HashMap<>();
		
		this.addOfferedInterface(ComputerPoolNetworkMasterI.class);
		this.cpnmip = new ComputerPoolNetworkMasterInboundPort(computerPoolInboundPortUri, this);
		this.cpnmip.publishPort();
		this.addPort(cpnmip);
		
		// Debug
		this.toggleLogging();
		this.toggleTracing();
		
		this.logMessage(uri + " has been created!");
	}

	@Override
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) {
		pools.put(computerPoolUri, compterPoolInboundPortUri);
		
	}

	@Override
	public void unregisterComputerPool(String computerPoolUri) {
		pools.remove(computerPoolUri);
	}

	@Override
	public HashMap<String, String> getAvailableComputerPools() {
		return pools;
	}

}
