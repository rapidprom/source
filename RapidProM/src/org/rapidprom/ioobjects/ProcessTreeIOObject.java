package org.rapidprom.ioobjects;

import com.rapidminer.ioobjects.ProMIOObject;
import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;
import org.rapidprom.ioobjectrenderers.ProcessTreeIOObjectVisualizationType;

public class ProcessTreeIOObject extends ResultObjectAdapter implements ProMIOObject{

	private static final long serialVersionUID = 1L;

	private ProcessTreeIOObjectVisualizationType vt = ProcessTreeIOObjectVisualizationType.DEFAULT;
	private PluginContext pc = null;
	private ProcessTree processTree = null;
	
	public ProcessTreeIOObject(){		
	}

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
	
	public void setVisualizationType(ProcessTreeIOObjectVisualizationType vt) {
		this.vt = vt;
	}
	
	public ProcessTreeIOObjectVisualizationType getVisualizationType () {
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
