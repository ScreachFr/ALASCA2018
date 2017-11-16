package fr.upmc.gaspardleo.cvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.cvm.CVMComponent;
import fr.upmc.gaspardleo.cvm.interfaces.CVMI;

public class CVMInboundPort extends AbstractInboundPort implements CVMI {
	private static final long serialVersionUID = 7849610987533143498L;


	public CVMInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CVMI.class, owner);
	}
	
	public CVMInboundPort(ComponentI owner) throws Exception {
		super(CVMI.class, owner);
	}

	@Override
	public void deployComponent(final ComponentI cmp) throws Exception {
		final CVMComponent cvmc = (CVMComponent) this.owner;


		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.deployComponent(cmp);
				return cvmc;
			}
		});
	}


	@Override
	public void allocateCores(final ApplicationVMManagementOutboundPort avmPort) throws Exception {
		final CVMComponent cvmc = (CVMComponent) this.owner;

		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.allocateCores(avmPort);
				return cvmc;
			}
		});

	}


	@Override
	public void start() throws Exception {
		final CVMComponent cvmc = (CVMComponent) this.owner;

		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.start();
				return cvmc;
			}
		});		
	}


	@Override
	public void addAVMPort(final ApplicationVMManagementOutboundPort avmPort) throws Exception {
		final CVMComponent cvmc = (CVMComponent) this.owner;

		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.addAVMPort(avmPort);
				return cvmc;
			}
		});
	}

}
