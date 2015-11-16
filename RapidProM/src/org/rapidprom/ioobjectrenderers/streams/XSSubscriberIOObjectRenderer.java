package org.rapidprom.ioobjectrenderers.streams;

import javax.swing.JComponent;

import org.processmining.stream.core.visualizer.XSStreamSubscriberVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.streams.XSSubscriberIOObject;

public class XSSubscriberIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<XSSubscriberIOObject> {

	@Override
	public String getName() {
		return "XSSubscriber (Generic) Renderer";
	}

	@Override
	protected JComponent runVisualization(XSSubscriberIOObject obj) {
		return XSStreamSubscriberVisualizer
				.visualizeSubscriber(obj.getArtifact());
	}

}
