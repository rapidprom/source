package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.guidetreeminer.ClusterLogOutput;

public class ClusterLogOutputIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private ClusterLogOutput clusterLogOutput = null;

	public ClusterLogOutputIOObject (ClusterLogOutput clusterLogOutput) {
		this.clusterLogOutput = clusterLogOutput;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setClusterLogOutput(ClusterLogOutput clusterLogOutput) {
		this.clusterLogOutput = clusterLogOutput;
	}

	public ClusterLogOutput getClusterLogOutput() {
		return clusterLogOutput;
	}

	public String toResultString() {
		String extractName = clusterLogOutput.toString();
		return "ClusterLogOutputIOObject:" + extractName;
	}

	public ClusterLogOutput getData() {
		return clusterLogOutput;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.clusterLogOutput = null;
		
	}

}
