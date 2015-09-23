package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner.*;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner;

public class InteractiveMinerLauncherIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private InteractiveMinerLauncher interactiveMinerLauncher = null;

	public InteractiveMinerLauncherIOObject (InteractiveMinerLauncher interactiveMinerLauncher) {
		this.interactiveMinerLauncher = interactiveMinerLauncher;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setInteractiveMinerLauncher(InteractiveMinerLauncher interactiveMinerLauncher) {
		this.interactiveMinerLauncher = interactiveMinerLauncher;
	}

	public InteractiveMinerLauncher getInteractiveMinerLauncher() {
		return interactiveMinerLauncher;
	}

	public String toResultString() {
		String extractName = interactiveMinerLauncher.toString();
		return "InteractiveMinerLauncherIOObject:" + extractName;
	}

	public InteractiveMinerLauncher getData() {
		return interactiveMinerLauncher;
	}

}
