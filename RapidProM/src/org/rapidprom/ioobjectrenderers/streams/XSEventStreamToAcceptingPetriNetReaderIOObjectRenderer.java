package org.rapidprom.ioobjectrenderers.streams;

import java.awt.Component;

import org.processmining.stream.core.visualizer.XSReaderVisualizer;
import org.rapidprom.ioobjects.streams.XSEventStreamToAcceptingPetriNetReaderIOObject;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class XSEventStreamToAcceptingPetriNetReaderIOObjectRenderer
		extends AbstractRenderer {

	@Override
	public String getName() {
		return "XSEventStream To AcceptingPetriNet Reader Renderer";
	}

	@Override
	public Component getVisualizationComponent(Object renderable,
			IOContainer ioContainer) {
		if (renderable instanceof XSEventStreamToAcceptingPetriNetReaderIOObject) {
			XSEventStreamToAcceptingPetriNetReaderIOObject obj = (XSEventStreamToAcceptingPetriNetReaderIOObject) renderable;
			return XSReaderVisualizer.visualize(obj.getArtifact());
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return null;
	}

}
