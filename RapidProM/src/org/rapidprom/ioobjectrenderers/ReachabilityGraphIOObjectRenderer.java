package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.ReachabilityGraphIOObject;

public class ReachabilityGraphIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<ReachabilityGraphIOObject> {

	@Override
	public String getName() {
		return "Reachability Graph renderer";
	}

	@Override
	protected JComponent runVisualization(ReachabilityGraphIOObject artifact) {
		return ProMJGraphVisualizer.instance().visualizeGraph(
				artifact.getPluginContext(), artifact.getArtifact());
	}
}