package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class PNRepResultIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private PNRepResult pNRepResult = null;

	public PNRepResultIOObject (PNRepResult pNRepResult) {
		this.pNRepResult = pNRepResult;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setPNRepResult(PNRepResult pNRepResult) {
		this.pNRepResult = pNRepResult;
	}

	public PNRepResult getPNRepResult() {
		return pNRepResult;
	}

	public String toResultString() {
		String extractName = pNRepResult.toString();
		return "PNRepResultIOObject:" + extractName;
	}

	public PNRepResult getData() {
		return pNRepResult;
	}

}
