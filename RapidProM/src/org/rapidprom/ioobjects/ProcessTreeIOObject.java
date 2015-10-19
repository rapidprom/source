package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;
import org.rapidprom.ioobjectrenderers.ProcessTreeIOObjectVisualizationType;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class ProcessTreeIOObject extends AbstractRapidProMIOObject<ProcessTree> {

	private static final long serialVersionUID = 780816193914598555L;
	private ProcessTreeIOObjectVisualizationType vt = ProcessTreeIOObjectVisualizationType.DEFAULT;

	public ProcessTreeIOObject(ProcessTree t, PluginContext context) {
		super(t, context);
	}

	public void setVisualizationType(ProcessTreeIOObjectVisualizationType vt) {
		this.vt = vt;
	}

	public ProcessTreeIOObjectVisualizationType getVisualizationType() {
		return this.vt;
	}

}
