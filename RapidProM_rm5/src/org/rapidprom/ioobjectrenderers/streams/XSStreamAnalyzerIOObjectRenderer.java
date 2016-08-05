package org.rapidprom.ioobjectrenderers.streams;

import javax.swing.JComponent;

import org.processmining.streamanalysis.core.visualizer.XSStreamAnalyzerVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.streams.XSStreamAnalyzerIOObject;

public class XSStreamAnalyzerIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<XSStreamAnalyzerIOObject<?, ?, ?>> {

	@Override
	public String getName() {
		return "XSStreamAnalyzer Object Renderer";
	}

	@Override
	protected JComponent runVisualization(
			XSStreamAnalyzerIOObject<?, ?, ?> ioObject) {
		return XSStreamAnalyzerVisualizer.visualize(ioObject.getArtifact());
	}

}
