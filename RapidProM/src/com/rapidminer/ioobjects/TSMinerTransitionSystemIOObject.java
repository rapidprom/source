package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;

public class TSMinerTransitionSystemIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private TSMinerTransitionSystem tSMinerTransitionSystem = null;

	public TSMinerTransitionSystemIOObject (TSMinerTransitionSystem tSMinerTransitionSystem) {
		this.tSMinerTransitionSystem = tSMinerTransitionSystem;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setTSMinerTransitionSystem(TSMinerTransitionSystem tSMinerTransitionSystem) {
		this.tSMinerTransitionSystem = tSMinerTransitionSystem;
	}

	public TSMinerTransitionSystem getTSMinerTransitionSystem() {
		return tSMinerTransitionSystem;
	}

	public String toResultString() {
		String extractName = tSMinerTransitionSystem.toString();
		return "TSMinerTransitionSystemIOObject:" + extractName;
	}

	public TSMinerTransitionSystem getData() {
		return tSMinerTransitionSystem;
	}

}
