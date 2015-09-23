package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.transitionsystem.ReachabilityGraph;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ReachabilityGraphIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class ReachabilityGraphIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof ReachabilityGraphIOObject) {
			ReachabilityGraphIOObject object = (ReachabilityGraphIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		ReachabilityGraphIOObject object = (ReachabilityGraphIOObject) renderable;
		// try to get the visualizer in ProM
		ProMJGraphPanel promPanel = (ProMJGraphPanel) runVisualization(object.getData(), object.getPluginContext());
		ProMJGraph graph = Utilities.getSizedGraph(promPanel,desiredWidth,desiredHeight);
		
		return new DefaultComponentRenderable(graph);
	}

	public static JComponent runVisualization(ReachabilityGraph reachabilityGraph, PluginContext pc) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(reachabilityGraph);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

	public String getName() {
		return "ReachabilityGraphrenderer";
	}

}