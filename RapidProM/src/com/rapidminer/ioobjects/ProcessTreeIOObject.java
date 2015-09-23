package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;

public class ProcessTreeIOObject extends ResultObjectAdapter implements ProMIOObject{

	public enum VisualizationType {Processtree, DOT};
	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private ProcessTree processTree = null;
	private VisualizationType vt = VisualizationType.Processtree;

	public ProcessTreeIOObject (ProcessTree processTree) {
		this.processTree = processTree;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setProcessTree(ProcessTree processTree) {
		this.processTree = processTree;
	}

	public ProcessTree getProcessTree() {
		return processTree;
	}

	public String toResultString() {
		String extractName = processTree.toString();
		return "ProcessTreeIOObject:" + extractName;
	}

	public ProcessTree getData() {
		return processTree;
	}
	
	public void setVisualizationType(VisualizationType vt) {
		this.vt = vt;
	}
	
	public VisualizationType getVisualizationType () {
		return this.vt;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		this.pc = null;
		this.processTree = null;
		this.vt = null;
	}

}
