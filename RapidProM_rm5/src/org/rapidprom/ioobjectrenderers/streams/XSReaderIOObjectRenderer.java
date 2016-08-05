package org.rapidprom.ioobjectrenderers.streams;

import javax.swing.JComponent;

import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.visualizers.XSReaderVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;

public class XSReaderIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<XSReaderIOObject<? extends XSDataPacket<?, ?>, ?>> {

	@Override
	public String getName() {
		return "XSReader Object Renderer";
	}

	@Override
	protected JComponent runVisualization(
			XSReaderIOObject<? extends XSDataPacket<?, ?>, ?> ioObject) {
		return XSReaderVisualizer.visualize(ioObject.getArtifact());
	}

}
