package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.BPMNIOObject;

public class BPMNIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<BPMNIOObject> {

	@Override
	protected JComponent runVisualization(BPMNIOObject artifact,
			PluginContext pluginContext) {
		return ProMJGraphVisualizer.instance().visualizeGraph(pluginContext,
				artifact.getArtifact());
	}

	@Override
	public String getName() {
		return "BPMN renderer";
	}

}