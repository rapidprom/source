package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.PNRepResultIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class PNRepResultIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof PNRepResultIOObject) {
			PNRepResultIOObject object = (PNRepResultIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultReadable("Not implemented yet.");
	}

	public static JComponent runVisualization(PNRepResult pNRepResult, PluginContext pc) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(pNRepResult);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

	public String getName() {
		return "PNRepResultrenderer";
	}

}