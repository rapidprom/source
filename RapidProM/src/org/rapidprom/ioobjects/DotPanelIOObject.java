package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class DotPanelIOObject extends AbstractRapidProMIOObject<DotPanel> {

	private static final long serialVersionUID = 7187144798295728319L;

	public DotPanelIOObject(DotPanel t, PluginContext context) {
		super(t, context);
	}

}