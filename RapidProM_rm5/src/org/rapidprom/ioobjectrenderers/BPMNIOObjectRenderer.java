package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.BPMNIOObject;

public class BPMNIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<BPMNIOObject> {

	@Override
	protected JComponent runVisualization(BPMNIOObject artifact) {
		return ProMJGraphVisualizer.instance().visualizeGraph(
				artifact.getPluginContext(), artifact.getArtifact());
	}

	@Override
	public String getName() {
		return "BPMN renderer";
	}

}