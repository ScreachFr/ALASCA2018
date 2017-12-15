package fr.upmc.gaspardleo.computerpool.ports;

import java.util.Map;
import java.util.Set;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM;
import fr.upmc.gaspardleo.computerpool.ComputerPool;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;

public class ComputerPoolInbounPort extends AbstractOutboundPort implements ComputerPoolI {




	public ComputerPoolInbounPort(String uri, ComponentI owner) throws Exception {
		super(uri, ComputerPoolI.class, owner);


	}

	@Override
	public void createNewComputer(String computerURI, Set<Integer> possibleFrequencies,
			Map<Integer, Integer> processingPower, int defaultFrequency, int maxFrequencyGap, int numberOfProcessors,
			int numberOfCores) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		computerPool.handleRequestAsync(
				new ComponentI.ComponentService<ComputerPool>(){
					@Override
					public ComputerPool call() throws Exception {
						computerPool.createNewComputer(computerURI, possibleFrequencies, processingPower,
								defaultFrequency, maxFrequencyGap, numberOfProcessors, numberOfCores);

						return computerPool;
					}});

	}

	@Override
	public ApplicationVM createNewApplicationVM(String avmURI, int numberOfCoreToAllocate) throws Exception {
		final ComputerPool computerPool = (ComputerPool)this.owner;
		return computerPool.handleRequestSync(
				new ComponentI.ComponentService<ApplicationVM>(){
					@Override
					public ApplicationVM call() throws Exception {
						return computerPool.createNewApplicationVM(avmURI, numberOfCoreToAllocate);

					}});
	}

}
