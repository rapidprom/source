package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.PetriNetIOObject;

public class PetriNetIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<PetriNetIOObject> {

	@Override
	public String getName() {
		return "Petri Net renderer";
	}

	@Override
	protected JComponent runVisualization(PetriNetIOObject artifact) {
		PetriNetVisualization visualizer = new PetriNetVisualization();
		return visualizer.visualize(artifact.getPluginContext(),
				artifact.getArtifact());
	}

}
