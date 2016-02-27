package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.ptandloggenerator.renderers.NewickTreeRenderer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.NewickTreeCollectionIOObject;

public class NewickTreeCollectionIOObjectRenderer extends AbstractRapidProMIOObjectRenderer<NewickTreeCollectionIOObject>{

	@Override
	public String getName() {
		return new String("NewickTree renderer");
	}

	@Override
	protected JComponent runVisualization(NewickTreeCollectionIOObject ioObject) {
		
		return NewickTreeRenderer.visualize(ioObject.getPluginContext(), ioObject.getArtifact());
		
	}

}
