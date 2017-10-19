package fr.upmc.gaspardleo.step1.step12.cvm;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.gaspardleo.step1.step12.cvm.interfaces.CVMI;

public class CVMComponent extends AbstractComponent implements CVMI {
	private AbstractCVM realCVM;
	
	public CVMComponent(AbstractCVM realCVM) {
		super(1, 1);
		this.realCVM = realCVM;
	}
	
	@Override
	public void deployComponent(ComponentI cmp) throws Exception {
		realCVM.addDeployedComponent(cmp);
	}

}
