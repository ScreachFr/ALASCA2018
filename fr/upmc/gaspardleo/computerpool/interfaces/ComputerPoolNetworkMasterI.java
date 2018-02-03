package fr.upmc.gaspardleo.computerpool.interfaces;

import java.util.HashMap;

public interface ComputerPoolNetworkMasterI {
	
	public void registerComputerPool(String computerPoolUri, String compterPoolInboundPortUri) throws Exception;
	
	public void unregisterComputerPool(String computerPoolUri) throws Exception;
	
	public HashMap<String, String> getAvailableComputerPools() throws Exception;
	
}
