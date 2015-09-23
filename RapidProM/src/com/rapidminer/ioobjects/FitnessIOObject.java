package com.rapidminer.ioobjects;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.operator.ResultObjectAdapter;

public class FitnessIOObject extends ResultObjectAdapter{

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private Double d = null;

	public FitnessIOObject (Double d) {
		this.d = d;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setInteger(Double d) {
		this.d = d;
	}

	public Double getInteger() {
		return d;
	}

	public String toResultString() {
		String extractName = d.toString();
		return "DoubleIOObject:" + extractName;
	}

	public Double getData() {
		return d;
	}
}
