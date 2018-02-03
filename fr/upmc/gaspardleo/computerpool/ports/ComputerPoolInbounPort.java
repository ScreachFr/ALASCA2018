package fr.upmc.gaspardleo.computerpool.ports;

import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.computer.Computer.ComputerPortsTypes;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolInbounPort 
		extends AbstractInboundPort
		implements ComputerPoolI {

	private static final long serialVersionUID = 1L;

	public ComputerPoolInbounPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolI.class, owner);
	}
	
	@Override
	public void addComputer(
			HashMap<ComputerPortsTypes, String> computerUris,
			Integer numberOfProcessors,
			Integer numberOfCores) throws Exception {

		final ComputerPool computerPool = (ComputerPool)this.owner;
		computerPool.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPool>(){
					@Override
					public ComputerPool call() throws Exception {
						computerPool.addComputer(
								computerUris,
								numberOfProcessors,
								numberOfCores);
						return computerPool;
					}});
	}

	@Override
	public HashMap<ApplicationVMPortTypes, String> createNewApplicationVM(
			String avmURI, 
			Integer numberOfCoreToAllocate) throws Exception {
		
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<HashMap<ApplicationVMPortTypes, String>>(){
					@Override
					public HashMap<ApplicationVMPortTypes, String> call() throws Exception {
						return computerPool.createNewApplicationVM(
								avmURI, 
								numberOfCoreToAllocate);
					}});
	}

	@Override
	public Boolean hasAvailableCore() throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return computerPool.hasAvailableCore();
					}});
	}

	@Override
	public Boolean increaseCoreFrequency(String avmUri) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return computerPool.increaseCoreFrequency(avmUri);
					}});
	}

	@Override
	public Boolean decreaseCoreFrequency(String avmUri) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<Boolean>(){
					@Override
					public Boolean call() throws Exception {
						return computerPool.decreaseCoreFrequency(avmUri);
					}});
	}

	@Override
	public void releaseCores(String avmUri) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		computerPool.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPool>(){
					@Override
					public ComputerPool call() throws Exception {
						computerPool.releaseCores(avmUri);
						return computerPool;
					}});
	}
}
