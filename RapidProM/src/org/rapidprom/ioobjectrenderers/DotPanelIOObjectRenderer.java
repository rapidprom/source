package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.videolectureanalysis.renderer.SequentialProcessRenderer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.DotPanelIOObject;

public class DotPanelIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<DotPanelIOObject> {

	@Override
	public String getName() {
		return "DotPanel renderer";
	}

	@Override
	protected JComponent runVisualization(DotPanelIOObject ioObject) {
		return SequentialProcessRenderer.visualize(ioObject.getPluginContext(),
				ioObject.getArtifact());
	}

}
