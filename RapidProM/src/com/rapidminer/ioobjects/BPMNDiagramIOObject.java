package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

public class BPMNDiagramIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private BPMNDiagram bPMNDiagram = null;

	public BPMNDiagramIOObject (BPMNDiagram bPMNDiagram) {
		this.bPMNDiagram = bPMNDiagram;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setBPMNDiagram(BPMNDiagram bPMNDiagram) {
		this.bPMNDiagram = bPMNDiagram;
	}

	public BPMNDiagram getBPMNDiagram() {
		return bPMNDiagram;
	}

	public String toResultString() {
		String extractName = bPMNDiagram.toString();
		return "BPMNDiagramIOObject:" + extractName;
	}

	public BPMNDiagram getData() {
		return bPMNDiagram;
	}

}
