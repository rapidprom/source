package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.fuzzymodel.FastTransformerVisualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.MetricsRepositoryIOObject;

public class MetricsRepositoryIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<MetricsRepositoryIOObject> {

	@Override
	public String getName() {
		return "Transition System (metrics repository) renderer";
	}

	@Override
	protected JComponent runVisualization(MetricsRepositoryIOObject artifact) {
		FastTransformerVisualization visualizer = new FastTransformerVisualization();
		return visualizer.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}