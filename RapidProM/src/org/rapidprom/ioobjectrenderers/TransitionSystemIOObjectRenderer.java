package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.transitionsystem.Visualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.TransitionSystemIOObject;

public class TransitionSystemIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<TransitionSystemIOObject> {

	@Override
	public String getName() {
		return "TransitionSystemRenderer";
	}

	@Override
	protected JComponent runVisualization(TransitionSystemIOObject artifact) {
		
		Visualization visualizator = new Visualization();
		return visualizator.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}