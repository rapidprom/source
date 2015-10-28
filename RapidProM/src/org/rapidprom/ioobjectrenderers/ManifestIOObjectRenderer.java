package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.manifestanalysis.visualization.performance.ManifestPerfVisualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.ManifestIOObject;

public class ManifestIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<ManifestIOObject> {

	@Override
	public String getName() {
		return "Manifest renderer";
	}

	@Override
	protected JComponent runVisualization(ManifestIOObject artifact) {
		ManifestPerfVisualization visualizer = new ManifestPerfVisualization();
		return visualizer.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}
