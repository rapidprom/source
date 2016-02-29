package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.ptandloggenerator.models.NewickTreeCollection;
import org.processmining.ptandloggenerator.renderers.NewickTreeRenderer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.NewickTreeIOObject;

public class NewickTreeIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<NewickTreeIOObject> {

	@Override
	public String getName() {
		return new String("NewickTree renderer");
	}

	@Override
	protected JComponent runVisualization(NewickTreeIOObject ioObject) {

		NewickTreeCollection collection = new NewickTreeCollection();
		collection.addNewickTree(ioObject.getArtifact());
		return NewickTreeRenderer.visualize(ioObject.getPluginContext(),
				collection);
	}

}
