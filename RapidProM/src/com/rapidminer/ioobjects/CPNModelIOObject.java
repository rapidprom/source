package com.rapidminer.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.cpnet.ColouredPetriNet;

import com.rapidminer.operator.ResultObjectAdapter;

public class CPNModelIOObject extends ResultObjectAdapter implements ProMIOObject {
	
	/**
	 * generated
	 */
	private static final long serialVersionUID = -7924883865640486269L;
	private ColouredPetriNet pn = null;
	// needed for rendering
	private PluginContext pc = null;
	
	public CPNModelIOObject (ColouredPetriNet pn) {
		this.pn = pn;
	}

	public ColouredPetriNet getPn() {
		return pn;
	}

	public void setPn(ColouredPetriNet pn) {
		this.pn = pn;
	}
	
	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}
	
	public PluginContext getPluginContext () {
		return this.pc;
	}
	
	@Override
	public String toResultString() {
		String extractName = pn.toString();
		return "ProMContextIOObject:" + extractName;
	}

	public ColouredPetriNet getData() {
		return this.pn;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.pn = null;
	}

}