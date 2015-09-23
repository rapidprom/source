package com.rapidminer.ioobjectrenderers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.rapidprom.prom.CallProm;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.ioobjects.DotPanelIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class DotPanelIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof DotPanelIOObject) {
			DotPanelIOObject object = (DotPanelIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof DotPanelIOObject) {
			
			DotPanelIOObject object = (DotPanelIOObject)  renderable;
			// try to get the visualizer in ProM
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			// put the thing in its own panel
			return new DefaultComponentRenderable(Utilities.getSizedPanel(panel,panel, desiredWidth, desiredHeight-50));
		}
		return new DefaultReadable("Not implemented yet.");
	}

	public static JComponent runVisualization(DotPanel panel, PluginContext pc) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(panel.getDot());
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

	public String getName() {
		return "DotPanelRenderer";
	}
}
