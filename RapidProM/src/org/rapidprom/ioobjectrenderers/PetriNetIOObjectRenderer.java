package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.PetriNetIOObject;

public class PetriNetIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<PetriNetIOObject> {

	@Override
	public String getName() {
		return "Petri Net renderer";
	}

	@Override
	protected JComponent runVisualization(PetriNetIOObject artifact) {
		PetriNetVisualization visualizer = new PetriNetVisualization();
		if (artifact.getArtifact() instanceof Petrinet)
			return visualizer.visualize(artifact.getPluginContext(),
					(Petrinet) artifact.getArtifact());
		if (artifact.getArtifact() instanceof ResetInhibitorNet)
			return visualizer.visualize(artifact.getPluginContext(),
					(ResetInhibitorNet) artifact.getArtifact());
		if (artifact.getArtifact() instanceof ResetNet)
			return visualizer.visualize(artifact.getPluginContext(),
					(ResetNet) artifact.getArtifact());
		if (artifact.getArtifact() instanceof InhibitorNet)
			return visualizer.visualize(artifact.getPluginContext(),
					(InhibitorNet) artifact.getArtifact());
		else
			return null;
	}

}
