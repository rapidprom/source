package org.rapidprom.ioobjectrenderers.streams;

import javax.swing.JComponent;

import org.processmining.stream.core.visualizer.XSStreamPublisherVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.streams.XSPublisherIOObject;

public class XSPublisherIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<XSPublisherIOObject> {

	@Override
	public String getName() {
		return "XSPublisher renderer";
	}

	@Override
	protected JComponent runVisualization(XSPublisherIOObject obj) {
		return XSStreamPublisherVisualizer
				.visualizePublisher(obj.getArtifact());
	}

}
