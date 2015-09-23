package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;

public class ReachabilityGraphIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private ReachabilityGraph reachabilityGraph = null;

	public ReachabilityGraphIOObject (ReachabilityGraph reachabilityGraph) {
		this.reachabilityGraph = reachabilityGraph;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setReachabilityGraph(ReachabilityGraph reachabilityGraph) {
		this.reachabilityGraph = reachabilityGraph;
	}

	public ReachabilityGraph getReachabilityGraph() {
		return reachabilityGraph;
	}

	public String toResultString() {
		String extractName = reachabilityGraph.toString();
		return "ReachabilityGraphIOObject:" + extractName;
	}

	public ReachabilityGraph getData() {
		return reachabilityGraph;
	}

}
