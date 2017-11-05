package fr.upmc.gaspardleo.step1.step12.cvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.gaspardleo.step1.step12.cvm.CVMComponent;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;

public class CVMInboundPort extends AbstractInboundPort implements CVMI {
	private static final long serialVersionUID = 7849610987533143498L;


	public CVMInboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CVMI.class, owner);
	}


	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		CVMComponent cvmc = (CVMComponent) this.owner;


		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.deployComponent(cmp);
				return cvmc;
			}
		});
	}


	@Override
	public void allocateCores(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		CVMComponent cvmc = (CVMComponent) this.owner;

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
		CVMComponent cvmc = (CVMComponent) this.owner;

		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.start();
				return cvmc;
			}
		});		
	}


	@Override
	public void addAVMPort(ApplicationVMManagementOutboundPort avmPort) throws Exception {
		CVMComponent cvmc = (CVMComponent) this.owner;

		cvmc.handleRequestAsync(new ComponentService<CVMI>() {

			@Override
			public CVMI call() throws Exception {
				cvmc.addAVMPort(avmPort);
				return cvmc;
			}
		});
	}

}
