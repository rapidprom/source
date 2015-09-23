package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.pompom.PomPomView;

public class PomPomViewIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private PomPomView pomPomView = null;

	public PomPomViewIOObject (PomPomView pomPomView) {
		this.pomPomView = pomPomView;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setPomPomView(PomPomView pomPomView) {
		this.pomPomView = pomPomView;
	}

	public PomPomView getPomPomView() {
		return pomPomView;
	}

	public String toResultString() {
		String extractName = pomPomView.toString();
		return "PomPomViewIOObject:" + extractName;
	}

	public PomPomView getData() {
		return pomPomView;
	}

}
