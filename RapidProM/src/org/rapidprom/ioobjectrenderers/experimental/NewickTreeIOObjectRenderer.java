package org.rapidprom.ioobjectrenderers.experimental;

import javax.swing.JComponent;

import org.processmining.ptandloggenerator.models.NewickTreeCollection;
import org.processmining.ptandloggenerator.renderers.NewickTreeRenderer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.experimental.NewickTreeIOObject;

public class NewickTreeIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<NewickTreeIOObject> {

	@Override
	protected JComponent runVisualization(NewickTreeIOObject artifact) {
		NewickTreeCollection collection = new NewickTreeCollection();
		collection.addNewickTree(artifact.getArtifact());
		return NewickTreeRenderer.visualize(artifact.getPluginContext(), collection );
	}

	@Override
	public String getName() {
		return "BPMN renderer";
	}

}