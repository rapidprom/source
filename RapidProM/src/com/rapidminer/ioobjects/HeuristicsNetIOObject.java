package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.heuristics.HeuristicsNet;

public class HeuristicsNetIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private HeuristicsNet heuristicsNet = null;

	public HeuristicsNetIOObject (HeuristicsNet heuristicsNet) {
		this.heuristicsNet = heuristicsNet;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setHeuristicsNet(HeuristicsNet heuristicsNet) {
		this.heuristicsNet = heuristicsNet;
	}

	public HeuristicsNet getHeuristicsNet() {
		return heuristicsNet;
	}

	public String toResultString() {
		String extractName = heuristicsNet.toString();
		return "HeuristicsNetIOObject:" + extractName;
	}

	public HeuristicsNet getData() {
		return heuristicsNet;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.heuristicsNet = null;		
	}

}
