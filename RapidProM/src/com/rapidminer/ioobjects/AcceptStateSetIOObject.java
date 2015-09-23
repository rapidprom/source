package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;

public class AcceptStateSetIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private AcceptStateSet acceptStateSet = null;

	public AcceptStateSetIOObject (AcceptStateSet acceptStateSet) {
		this.acceptStateSet = acceptStateSet;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setAcceptStateSet(AcceptStateSet acceptStateSet) {
		this.acceptStateSet = acceptStateSet;
	}

	public AcceptStateSet getAcceptStateSet() {
		return acceptStateSet;
	}

	public String toResultString() {
		String extractName = acceptStateSet.toString();
		return "AcceptStateSetIOObject:" + extractName;
	}

	public AcceptStateSet getData() {
		return acceptStateSet;
	}

}
