package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.models.animation.visualization.AnimationVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.FuzzyAnimationIOObject;

public class FuzzyAnimationIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<FuzzyAnimationIOObject> {

	@Override
	public String getName() {
		return "FuzzyAnimation renderer";
	}

	@Override
	protected JComponent runVisualization(FuzzyAnimationIOObject artifact) {
		AnimationVisualizer visualizer = new AnimationVisualizer();
		return visualizer.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}