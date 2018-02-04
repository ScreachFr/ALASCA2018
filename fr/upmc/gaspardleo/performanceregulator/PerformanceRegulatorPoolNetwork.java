package fr.upmc.gaspardleo.performanceregulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolNetworkMasterI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolNetworkMasterOutboundPort;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator.PerformanceRegulatorPorts;
import fr.upmc.gaspardleo.performanceregulator.PerformanceRegulator.RegulationStrategies;
import fr.upmc.gaspardleo.performanceregulator.data.TargetValue;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;
import fr.upmc.gaspardleo.performanceregulator.ports.PerformanceRegulatorInboundPort;
import fr.upmc.gaspardleo.performanceregulator.strategies.SimpleAVMStrategie;
import fr.upmc.gaspardleo.performanceregulator.strategies.SimpleFrequencyStrategy;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherOutboundPort;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor.RequestMonitorPorts;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorOutboundPort;

public class PerformanceRegulatorPoolNetwork 
extends AbstractComponent 
implements PerformanceRegulatorI {

	private static int DEBUG_LEVEL = 2;
	private static int newAVMID = 0;

	public final static double CONTROL_FEQUENCY = 30; // Based on a minute.
	public final static long REGULATION_TRUCE = 30000; // 30 sec.
	public final static long FIRST_PERF_CHECK = 1000;

	private String uri;

	// Hardware providers
	private ComputerPoolNetworkMasterOutboundPort cpnmop;
	private HashMap<String, ComputerPoolOutboundPort> cpops;
	private String computerPoolConnectorCanonicalName;

	// AVMs
	private HashMap<String, String> avmsOrigin;

	// Ports
	private PerformanceRegulatorInboundPort prip;
	private RequestMonitorOutboundPort rmop;
	private RequestDispatcherOutboundPort rdop;

	// Regulation
	private RegulationStrategyI strategy;
	private TargetValue targetValue;

	public PerformanceRegulatorPoolNetwork(
			String uri,
			HashMap<PerformanceRegulatorPorts, String> component_uris,
			HashMap<RDPortTypes, String> requestDispatcher,
			HashMap<RequestMonitorPorts, String> requestMonitor,
			String computerPoolNetworkMasterInboundPort_uri,
			RegulationStrategies strategy,
			TargetValue targetValue)
					throws Exception {

		super(1, 1);

		this.uri = uri;

		this.uri = component_uris.get(PerformanceRegulatorPorts.INTROSPECTION);
		this.strategy = getStrategyFromEnum(strategy);
		this.targetValue = targetValue;

		this.addOfferedInterface(PerformanceRegulatorI.class);
		this.prip = new PerformanceRegulatorInboundPort(component_uris.get(PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN), this);
		this.addPort(this.prip);
		this.prip.publishPort();

		//Request monitor port creation and connection.
		this.addRequiredInterface(RequestMonitorI.class);
		this.rmop = new RequestMonitorOutboundPort(this);
		this.addPort(rmop);
		this.rmop.publishPort();

		this.rmop.doConnection(
				requestMonitor.get(RequestMonitorPorts.REQUEST_MONITOR_IN),
				ClassFactory.newConnector(RequestMonitorI.class).getCanonicalName());

		//Request dispatcher port creation and connection.
		this.addRequiredInterface(RequestDispatcherI.class);
		this.rdop = new RequestDispatcherOutboundPort(this);
		this.addPort(rdop);
		this.rdop.publishPort();

		this.rdop.doConnection(
				requestDispatcher.get(RDPortTypes.REQUEST_DISPATCHER_IN),
				ClassFactory.newConnector(RequestDispatcherI.class).getCanonicalName());

		this.addRequiredInterface(ComputerPoolNetworkMasterI.class);
		this.cpnmop = new ComputerPoolNetworkMasterOutboundPort(AbstractPort.generatePortURI(), this);

		this.addPort(this.cpnmop);
		this.cpnmop.publishPort();
		this.cpnmop.doConnection(
				computerPoolNetworkMasterInboundPort_uri,
				ClassFactory.newConnector(ComputerPoolNetworkMasterI.class).getCanonicalName()
				);


		this.addRequiredInterface(ComputerPoolI.class);

		this.cpops = new HashMap<>();
		this.computerPoolConnectorCanonicalName = ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName();
		this.avmsOrigin = new HashMap<>();

		// Debug
		this.toggleLogging();
		this.toggleTracing();
	}


	@Override
	public Boolean addAVMToRD() throws Exception {
		Optional<HashMap<ApplicationVMPortTypes, String>> avmToAdd;
		
		String newAVMUri = "avm-"+(newAVMID++);
		
		for (Entry<String, ComputerPoolOutboundPort> e : cpops.entrySet()) {
			avmToAdd = getAVMFromCpop(newAVMUri, 1, e.getValue());
			
			if (avmToAdd.isPresent()) {
				avmsOrigin.put(e.getKey(), avmToAdd.get().get(ApplicationVMPortTypes.INTROSPECTION));
				rdop.registerVM(avmToAdd.get(), RequestSubmissionI.class);
				
				return true;
			}
		}
		
		this.logMessage(this.uri + " : addAVMToRD : No available ressource!");
		
		return false;
	}
	
	private Optional<HashMap<ApplicationVMPortTypes, String>> getAVMFromCpop(
			String avmUri,
			Integer numberOfCoreToAllocate,
			ComputerPoolOutboundPort cpop) throws Exception {
		
		return Optional.ofNullable(cpop.createNewApplicationVM(avmUri, numberOfCoreToAllocate));
	}

	@Override
	public Boolean removeAVMFromRD() throws Exception {
		List<String> avms = rdop.getRegisteredAVMUris();
		
		if (avms.size() <= 1) {
			this.logMessage(this.uri + " : Can't remove any AVM : there's too few left in RD.");
			return false;
		}
		
		String avmToRemove = avms.remove(0);
		
		rdop.unregisterVM(avmToRemove);
		cpops.get(avmsOrigin.get(avmToRemove)).releaseCores(avmToRemove);

		return true;
	}

	@Override
	public Boolean increaseCPUFrequency() throws Exception {
		Boolean hasChangedFreq = false;

		List<String> avms;

		avms = rdop.getRegisteredAVMUris();

		for (String avm : avms) {
			if (cpops.get(avmsOrigin.get(avm)).increaseCoreFrequency(avm))
				hasChangedFreq = true;
		}

		return hasChangedFreq;
	}

	@Override
	public Boolean decreaseCPUFrequency() throws Exception {
		Boolean hasChangedFreq = false;

		List<String> avms;

		avms = rdop.getRegisteredAVMUris();

		for (String avm : avms) {
			if (cpops.get(avmsOrigin.get(avm)).decreaseCoreFrequency(avm))
				hasChangedFreq = true;
		}

		return hasChangedFreq;
	}

	private void updateCpops() throws Exception {
		HashMap<String, String> availablePools = cpnmop.getAvailableComputerPools();

		// Removes ComputerPools that are not available anymore from cpops .
		cpops.keySet().stream()
		.filter(e -> !availablePools.containsKey(e))
		.forEach(e -> {
			try {
				cpops.get(e).doDisconnection();
				cpops.remove(e);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});



		// Creates ports if needed.
		for (Entry<String, String> e : availablePools.entrySet()) {
			if (!cpops.containsKey(e.getKey())) {
				ComputerPoolOutboundPort cpop = new ComputerPoolOutboundPort(this);
				this.addPort(cpop);
				cpop.publishPort();
				cpop.doConnection(e.getValue(), computerPoolConnectorCanonicalName);
				cpops.put(e.getKey(), cpop);
			}
		}


	}


	@Override
	public void setRegulationStrategie(RegulationStrategyI strat) throws Exception {
		this.strategy = strat;
	}


	@Override
	public RegulationStrategyI getRegulationStrategie() throws Exception {
		return strategy;
	}

	@Override
	public void start() throws ComponentStartException {
		super.start();
		try {
			this.startRegulationControlLoop();
		} catch(Exception e) {
			throw new ComponentStartException(e);
		}
	}


	@Override
	public void startRegulationControlLoop() throws Exception {
		if (DEBUG_LEVEL > 0)
			this.logMessage(this.uri + " : Regulation is active!");

		this.scheduleTaskAtFixedRate(() -> {
			try {
				Double mean = rmop.getMeanRequestExecutionTime();

				if (DEBUG_LEVEL > 1)
					this.logMessage(uri + " : current mean : " + mean + "ms.");

				if (!rmop.isDataRelevant()) {
					if (DEBUG_LEVEL > 1)
						this.logMessage(uri + " : The probe is telling me that the data is irrelevant, skipping.");

				} else if (mean > targetValue.getUpperBound()) {
					if (DEBUG_LEVEL > 1)
						this.logMessage(uri + " : upper bound regulation.");
					strategy.increasePerformances(this);

					// Permet de ne pas reverifier l'état du système trop rapidement après régulation.
					Thread.sleep(REGULATION_TRUCE);
				} else if (mean < targetValue.getLowerBound()) {
					if (DEBUG_LEVEL > 1)
						this.logMessage(uri + " : lower bound regulation.");

					strategy.decreasePerformances(this);

					Thread.sleep(REGULATION_TRUCE);
				} else {
					if (DEBUG_LEVEL > 1)
						this.logMessage(uri + " : everything seems within bounds, no regullation needed.");
				}

				
				updateCpops();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, FIRST_PERF_CHECK, (long) (60000 / CONTROL_FEQUENCY), TimeUnit.MILLISECONDS);


	}

	private RegulationStrategyI getStrategyFromEnum(RegulationStrategies strat) {
		switch(strat) {
		case SIMPLE_AVM :
			return new SimpleAVMStrategie();
		case SIMPLE_FREQ :
			return new SimpleFrequencyStrategy();
		case STRATEGY_TO_SURPASS_METAL_GEAR:
			throw new Error("Such a lust for revenge. WHO? (Not implemented yet)"); // Yes, I'm making bad jokes about video games when I'm too tired to work.
		default :
			throw new Error("Performance regulator constructor error : Strategy selection error. This shouldn't happen though.");
		}
	}

	public static HashMap<PerformanceRegulatorPorts, String> makeUris(String rd_URI){
		HashMap<PerformanceRegulatorPorts, String> performanceRegulator_uris = new HashMap<>();
		performanceRegulator_uris.put(PerformanceRegulatorPorts.INTROSPECTION, rd_URI + "-pr");
		performanceRegulator_uris.put(PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN, AbstractPort.generatePortURI());
		return performanceRegulator_uris;
	}

}
