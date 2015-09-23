package com.rapidminer.ioobjects;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import com.rapidminer.operator.ResultObjectAdapter;

/**
 * 
 * @author rmans
 *
 */
public class PetriNetIOObject extends ResultObjectAdapter implements ProMIOObject {
	
	/**
	 * generated
	 */
	private static final long serialVersionUID = -7924883864810486269L;
	private Petrinet pn = null;
	// needed for rendering
	private PluginContext pc = null;
	
	public PetriNetIOObject (Petrinet pn) {
		this.pn = pn;
	}

	public Petrinet getPn() {
		return pn;
	}

	public void setPn(Petrinet pn) {
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
		String extractName = pn.getLabel();
		return "ProMContextIOObject:" + extractName;
	}

	public Petrinet getData() {
		return this.pn;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.pn = null;
	}

}
