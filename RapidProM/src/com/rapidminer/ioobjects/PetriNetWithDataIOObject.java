package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;

public class PetriNetWithDataIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private PetriNetWithData petriNetWithData = null;

	public PetriNetWithDataIOObject (PetriNetWithData petriNetWithData) {
		this.petriNetWithData = petriNetWithData;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setPetriNetWithData(PetriNetWithData petriNetWithData) {
		this.petriNetWithData = petriNetWithData;
	}

	public PetriNetWithData getPetriNetWithData() {
		return petriNetWithData;
	}

	public String toResultString() {
		String extractName = petriNetWithData.toString();
		return "PetriNetWithDataIOObject:" + extractName;
	}

	public PetriNetWithData getData() {
		return petriNetWithData;
	}

}
