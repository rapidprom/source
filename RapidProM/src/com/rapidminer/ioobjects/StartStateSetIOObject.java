package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;

public class StartStateSetIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private StartStateSet startStateSet = null;

	public StartStateSetIOObject (StartStateSet startStateSet) {
		this.startStateSet = startStateSet;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setStartStateSet(StartStateSet startStateSet) {
		this.startStateSet = startStateSet;
	}

	public StartStateSet getStartStateSet() {
		return startStateSet;
	}

	public String toResultString() {
		String extractName = startStateSet.toString();
		return "StartStateSetIOObject:" + extractName;
	}

	public StartStateSet getData() {
		return startStateSet;
	}

}
