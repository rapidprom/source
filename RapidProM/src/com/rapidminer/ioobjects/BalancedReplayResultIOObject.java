package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.balancedconformance.result.BalancedReplayResult;

public class BalancedReplayResultIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private BalancedReplayResult balancedReplayResult = null;

	public BalancedReplayResultIOObject (BalancedReplayResult balancedReplayResult) {
		this.balancedReplayResult = balancedReplayResult;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setBalancedReplayResult(BalancedReplayResult balancedReplayResult) {
		this.balancedReplayResult = balancedReplayResult;
	}

	public BalancedReplayResult getBalancedReplayResult() {
		return balancedReplayResult;
	}

	public String toResultString() {
		String extractName = balancedReplayResult.toString();
		return "BalancedReplayResultIOObject:" + extractName;
	}

	public BalancedReplayResult getData() {
		return balancedReplayResult;
	}

}
