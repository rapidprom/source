package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.petrinet.replayresult.visualization.PNLogReplayResultVis;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.PNRepResultIOObject;

public class PNRepResultIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<PNRepResultIOObject> {

	@Override
	public String getName() {
		return "PNRepResult renderer";
	}

	@Override
	protected JComponent runVisualization(PNRepResultIOObject artifact) {
		PNLogReplayResultVis visualizer = new PNLogReplayResultVis();
		return visualizer.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}