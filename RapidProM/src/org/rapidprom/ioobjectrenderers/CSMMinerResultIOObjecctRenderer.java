package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.csmminer.plugins.CSMMinerVisualisationPlugin;
import org.processmining.plugins.transitionsystem.Visualization;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.CSMMinerResultIOObject;
import org.rapidprom.ioobjects.TransitionSystemIOObject;

public class CSMMinerResultIOObjecctRenderer extends AbstractRapidProMIOObjectRenderer<CSMMinerResultIOObject> {

	@Override
	public String getName() {
		return "CSMMinerResultRenderer";
	}

	@Override
	protected JComponent runVisualization(CSMMinerResultIOObject ioObject) {
		CSMMinerVisualisationPlugin visualisation = new CSMMinerVisualisationPlugin();
		return visualisation.visualize(ioObject.getPluginContext(), ioObject.getArtifact());
	}

}
