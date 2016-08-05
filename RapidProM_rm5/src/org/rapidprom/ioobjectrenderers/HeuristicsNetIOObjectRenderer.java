package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.heuristicsnet.visualizer.HeuristicsNetAnnotatedVisualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.HeuristicsNetIOObject;

public class HeuristicsNetIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<HeuristicsNetIOObject> {

	public String getName() {
		return "Heuristics Net renderer";
	}

	@Override
	protected JComponent runVisualization(HeuristicsNetIOObject artifact) {
		return HeuristicsNetAnnotatedVisualization.visualize(
				artifact.getPluginContext(), artifact.getArtifact());
	}

}
