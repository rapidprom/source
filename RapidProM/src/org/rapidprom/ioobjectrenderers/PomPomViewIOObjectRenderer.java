package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.pompom.PomPomVisualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.PomPomViewIOObject;

public class PomPomViewIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<PomPomViewIOObject> {

	@Override
	public String getName() {
		return "PomPomView renderer";
	}

	@Override
	protected JComponent runVisualization(PomPomViewIOObject artifact) {
		PomPomVisualization visualizer = new PomPomVisualization();
		return visualizer.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}