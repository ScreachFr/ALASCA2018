package fr.upmc.gaspardleo.componentmanagement;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface ShutdownableI 
		extends	OfferedI, RequiredI{
	public void shutdown() throws Exception;
}
