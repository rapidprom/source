package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;

public class DirectedGraphElementWeightsIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private DirectedGraphElementWeights directedGraphElementWeights = null;

	public DirectedGraphElementWeightsIOObject (DirectedGraphElementWeights directedGraphElementWeights) {
		this.directedGraphElementWeights = directedGraphElementWeights;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setDirectedGraphElementWeights(DirectedGraphElementWeights directedGraphElementWeights) {
		this.directedGraphElementWeights = directedGraphElementWeights;
	}

	public DirectedGraphElementWeights getDirectedGraphElementWeights() {
		return directedGraphElementWeights;
	}

	public String toResultString() {
		String extractName = directedGraphElementWeights.toString();
		return "DirectedGraphElementWeightsIOObject:" + extractName;
	}

	public DirectedGraphElementWeights getData() {
		return directedGraphElementWeights;
	}

}
