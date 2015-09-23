package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.semantics.petrinet.Marking;

public class MarkingIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private Marking marking = null;

	public MarkingIOObject (Marking marking) {
		this.marking = marking;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setMarking(Marking marking) {
		this.marking = marking;
	}

	public Marking getMarking() {
		return marking;
	}

	public String toResultString() {
		String extractName = marking.toString();
		return "MarkingIOObject:" + extractName;
	}

	public Marking getData() {
		return marking;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.marking = null;
	}

}
