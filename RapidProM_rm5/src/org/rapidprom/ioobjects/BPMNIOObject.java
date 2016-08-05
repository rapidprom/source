package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class BPMNIOObject extends AbstractRapidProMIOObject<BPMNDiagram> {

	private static final long serialVersionUID = -6452670552752770876L;

	public BPMNIOObject(BPMNDiagram t, PluginContext context) {
		super(t, context);
	}
}
