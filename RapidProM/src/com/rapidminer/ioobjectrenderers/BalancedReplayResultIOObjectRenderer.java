package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.balancedconformance.result.BalancedReplayResult;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.BalancedReplayResultIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class BalancedReplayResultIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof BalancedReplayResultIOObject) {
			BalancedReplayResultIOObject object = (BalancedReplayResultIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultReadable("Not implemented yet.");
	}

	public static JComponent runVisualization(BalancedReplayResult balancedReplayResult, PluginContext pc) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(balancedReplayResult);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

	public String getName() {
		return "BalancedReplayResultrenderer";
	}

}