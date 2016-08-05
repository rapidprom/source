package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.AcceptingPetriNetIOObject;

public class AcceptingPetriNetIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<AcceptingPetriNetIOObject> {

	@Override
	public String getName() {
		return "Accepting Petri Net Object Renderer";
	}

	@Override
	protected JComponent runVisualization(AcceptingPetriNetIOObject ioObject) {
		return VisualizeAcceptingPetriNetPlugin
				.visualize(ioObject.getPluginContext(), ioObject.getArtifact());
	}

}
