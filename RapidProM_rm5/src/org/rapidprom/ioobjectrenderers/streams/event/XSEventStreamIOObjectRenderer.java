package org.rapidprom.ioobjectrenderers.streams.event;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSStream;
import org.processmining.stream.core.visualizers.XSStreamVisualizer;
import org.rapidprom.ioobjects.streams.event.XSEventStreamIOObject;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class XSEventStreamIOObjectRenderer extends AbstractRenderer {

	private static String NAME = "XSEventStreamIOObjectRenderer renderer";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Component getVisualizationComponent(Object renderable,
			IOContainer ioContainer) {

		return new JLabel(getName());
	}

	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof XSEventStreamIOObject) {
			XSEventStreamIOObject object = (XSEventStreamIOObject) renderable;

			JComponent panel = runVisualization(object.getArtifact(),
					object.getPluginContext());
			// put the thing in its own panel
			return new DefaultComponentRenderable(panel);
		}
		return new DefaultReadable("No XSEventStream visualization available.");
	}

	private JComponent runVisualization(XSStream<XSEvent> artifact,
			PluginContext pluginContext) {
		return XSStreamVisualizer.visualize(pluginContext, artifact);
	}
}
