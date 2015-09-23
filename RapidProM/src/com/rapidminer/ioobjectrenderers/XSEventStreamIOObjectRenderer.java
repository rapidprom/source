package com.rapidminer.ioobjectrenderers;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.ioobjects.XSEventStreamIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class XSEventStreamIOObjectRenderer extends AbstractRenderer {

	private static String GIVEN_NAME = "XSEventStreamIOObjectRenderer renderer";
	
	@Override
	public String getName() {
		return GIVEN_NAME;
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

			JComponent panel = runVisualization(object.getData(),
					object.getPluginContext());
			// put the thing in its own panel
			return new DefaultComponentRenderable(Utilities.getSizedPanel(
					panel, panel, desiredWidth, desiredHeight - 50));
		}
		return new DefaultReadable("No XSEventStream visualization available.");
	}

	public static JComponent runVisualization(XSEventStream pn, PluginContext pc) {
		return new JLabel(GIVEN_NAME);
	}

}
