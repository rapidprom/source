package org.rapidprom.ioobjectrenderers.streams;

import javax.swing.JComponent;

import org.processmining.stream.core.visualizers.XSAuthorVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.streams.XSAuthorIOObject;

public class XSAuthorIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<XSAuthorIOObject<?>> {

	@Override
	public String getName() {
		return "XSAuthor Object Renderer";
	}

	@Override
	protected JComponent runVisualization(XSAuthorIOObject<?> artifact) {
		return XSAuthorVisualizer.visualize(artifact.getArtifact());
	}

}
