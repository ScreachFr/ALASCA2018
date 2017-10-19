package fr.upmc.gaspardleo.step1.step12.cvm.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.gaspardleo.step1.step12.cvm.CVMComponent;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;

public class CVMOutboundPort extends AbstractOutboundPort implements CVMI {

	public CVMOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, CVMI.class, owner);
	}

	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		((CVMComponent)this.owner).deployComponent(cmp);
	}

}
