package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;

public class MutableFuzzyGraphIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private MutableFuzzyGraph mutableFuzzyGraph = null;

	public MutableFuzzyGraphIOObject (MutableFuzzyGraph mutableFuzzyGraph) {
		this.mutableFuzzyGraph = mutableFuzzyGraph;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setMutableFuzzyGraph(MutableFuzzyGraph mutableFuzzyGraph) {
		this.mutableFuzzyGraph = mutableFuzzyGraph;
	}

	public MutableFuzzyGraph getMutableFuzzyGraph() {
		return mutableFuzzyGraph;
	}

	public String toResultString() {
		String extractName = mutableFuzzyGraph.toString();
		return "MutableFuzzyGraphIOObject:" + extractName;
	}

	public MutableFuzzyGraph getData() {
		return mutableFuzzyGraph;
	}

}
