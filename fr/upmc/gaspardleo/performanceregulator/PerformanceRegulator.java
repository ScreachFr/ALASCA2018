package fr.upmc.gaspardleo.performanceregulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.gaspardleo.applicationvm.ApplicationVM.ApplicationVMPortTypes;
import fr.upmc.gaspardleo.classfactory.ClassFactory;
import fr.upmc.gaspardleo.computerpool.ComputerPool.ComputerPoolPorts;
import fr.upmc.gaspardleo.computerpool.interfaces.ComputerPoolI;
import fr.upmc.gaspardleo.computerpool.ports.ComputerPoolOutboundPort;
import fr.upmc.gaspardleo.performanceregulator.data.TargetValue;
import fr.upmc.gaspardleo.performanceregulator.interfaces.PerformanceRegulatorI;
import fr.upmc.gaspardleo.performanceregulator.interfaces.RegulationStrategyI;
import fr.upmc.gaspardleo.performanceregulator.strategies.SimpleAVMStrategie;
import fr.upmc.gaspardleo.performanceregulator.strategies.SimpleFrequencyStrategy;
import fr.upmc.gaspardleo.requestdispatcher.RequestDispatcher.RDPortTypes;
import fr.upmc.gaspardleo.requestdispatcher.connectors.RequestDispatherConnector;
import fr.upmc.gaspardleo.requestdispatcher.interfaces.RequestDispatcherI;
import fr.upmc.gaspardleo.requestdispatcher.ports.RequestDispatcherOutboundPort;
import fr.upmc.gaspardleo.requestmonitor.RequestMonitor.RequestMonitorPorts;
import fr.upmc.gaspardleo.requestmonitor.interfaces.RequestMonitorI;
import fr.upmc.gaspardleo.requestmonitor.ports.RequestMonitorOutboundPort;

public class PerformanceRegulator extends AbstractComponent implements PerformanceRegulatorI {
	private static int DEBUG_LEVEL = 2;

	public final static double CONTROL_FEQUENCY = 30; // Based on a minute.
	public final static long REGULATION_TRUCE = 2000;
	public final static long FIRST_PERF_CHECK = 1000;

	private static int newAVMID = 0;

	public enum PerformanceRegulatorPorts {
		INTROSPECTION, PERFORMANCE_REGULATOR_IN;
	}

	public enum RegulationStrategies {
		SIMPLE_AVM, SIMPLE_FREQ, STRATEGY_TO_SURPASS_METAL_GEAR;
	}


	private String uri;

	// Ports
	private RequestMonitorOutboundPort rmop;
	private RequestDispatcherOutboundPort rdop;
	private ComputerPoolOutboundPort cpop;

	// Regulation
	private RegulationStrategyI strategy;
	private TargetValue targetValue;
	private Boolean isUsingUpperBound;

	public PerformanceRegulator(
			String componentURI,
			String performanceRegulator_in,
			HashMap<RDPortTypes, String> requestDispatcher,
			HashMap<RequestMonitorPorts, String> requestMonitor,
			HashMap<ComputerPoolPorts, String> computerPool,
			RegulationStrategies strategy,
			TargetValue targetValue) throws Exception {

		super(1, 1);

		this.uri = componentURI;

		this.strategy = getStrategyFromEnum(strategy);

		this.targetValue = targetValue;
		this.isUsingUpperBound = false;


		//Request monitor port creation and connection.
		this.addRequiredInterface(RequestMonitorI.class);
		this.rmop = new RequestMonitorOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(rmop);
		this.rmop.publishPort();

		this.rmop.doConnection(requestMonitor.get(RequestMonitorPorts.REQUEST_MONITOR_IN),
				ClassFactory.newConnector(RequestMonitorI.class).getCanonicalName());

		//Request dispatcher port creation and connection.
		this.addRequiredInterface(RequestDispatcherI.class);
		this.rdop = new RequestDispatcherOutboundPort(this);
		this.addPort(rdop);
		this.rdop.publishPort();

		this.rdop.doConnection(requestDispatcher.get(RDPortTypes.REQUEST_DISPATCHER_IN), 
				RequestDispatherConnector.class.getCanonicalName());

		//Computer pool port creation and connection.
		this.addRequiredInterface(ComputerPoolI.class);
		this.cpop = new ComputerPoolOutboundPort(AbstractPort.generatePortURI(), this);
		this.addPort(this.cpop);
		this.cpop.publishPort();

		this.cpop.doConnection(computerPool.get(ComputerPoolPorts.COMPUTER_POOL), 
				ClassFactory.newConnector(ComputerPoolI.class).getCanonicalName());

		//Debug
		this.toggleTracing();
		this.toggleLogging();
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


	@Override
	public Boolean increaseCPUFrequency() throws Exception {
		Boolean hasChangedFreq = false;
		
		List<String> avms = rdop.getRegisteredAVMUris();
		
		for (String avm : avms) {
			if (cpop.increaseCoreFrequency(avm))
				hasChangedFreq = true;
		}
		
		
		return hasChangedFreq;
	}


	@Override
	public Boolean decreaseCPUFrequency() throws Exception {
		Boolean hasChangedFreq = false;
		
		List<String> avms = rdop.getRegisteredAVMUris();
		
		for (String avm : avms) {
			if (cpop.decreaseCoreFrequency(avm))
				hasChangedFreq = true;
		}
		
		return hasChangedFreq;
	}


	@Override
	public Boolean addAVMToRD() throws Exception {
		Map<ApplicationVMPortTypes, String> avm = this.cpop.createNewApplicationVM("avm-"+(newAVMID++), 1);

		if (avm == null) {
			this.logMessage(this.uri + " : addAVMToRD : No available ressource!");

			return false;
		}

		rdop.registerVM(avm, RequestSubmissionI.class);

		return true;
	}


	@Override
	public Boolean removeAVMFromRD() {
		// TODO Auto-generated method stub
		return null;
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

			} catch (Exception e) {
				e.printStackTrace();
			}
		}, FIRST_PERF_CHECK, (long) (60000 / CONTROL_FEQUENCY), TimeUnit.MILLISECONDS);


	}

	public static HashMap<PerformanceRegulatorPorts, String> newInstance(DynamicComponentCreationOutboundPort dcc,
			String componentURI,
			HashMap<RDPortTypes, String> requestDispatcher,
			HashMap<RequestMonitorPorts, String> requestMonitor,
			HashMap<ComputerPoolPorts, String> computerPool,
			RegulationStrategies strategy, 
			TargetValue targetValue) throws Exception {

		String performanceRegulator_in = AbstractPort.generatePortURI();


		Object[] args = new Object[] {
				componentURI,
				performanceRegulator_in,
				requestDispatcher, 
				requestMonitor,
				computerPool,
				strategy,
				targetValue
		};

		try {
			dcc.createComponent(PerformanceRegulator.class.getCanonicalName(), args);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		HashMap<PerformanceRegulatorPorts, String> result = new HashMap<>();

		result.put(PerformanceRegulatorPorts.INTROSPECTION, componentURI);
		result.put(PerformanceRegulatorPorts.PERFORMANCE_REGULATOR_IN, performanceRegulator_in);

		return result;
	}

}
