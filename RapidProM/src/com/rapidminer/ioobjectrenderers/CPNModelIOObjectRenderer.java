package com.rapidminer.ioobjectrenderers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.rapidprom.prom.CallProm;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.ioobjects.CPNModelIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class CPNModelIOObjectRenderer extends AbstractRenderer {

	@Override
	public String getName() {
		return "Coloured Petri Net renderer";
	}

	@Override
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof CPNModelIOObject) {
			CPNModelIOObject object = (CPNModelIOObject) renderable;
			// try to get the visualizer in ProM
			JComponent panel = runVisualization(object.getPn(), object.getPluginContext());
			return panel;
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof CPNModelIOObject) {
			CPNModelIOObject object = (CPNModelIOObject) renderable;
			
			ProMJGraphPanel promPanel = (ProMJGraphPanel) runVisualization(object.getPn(), object.getPluginContext());
			ProMJGraph graph = Utilities.getSizedGraph(promPanel,desiredWidth,desiredHeight);
			//return new DefaultComponentRenderable(runVisualization(object.getPn(), object.getPluginContext()));
			return new DefaultComponentRenderable(graph);
		}
		return new DefaultReadable("No Petri Net visualization available.");
	}
	
	public static JComponent runVisualization(ColouredPetriNet pn, PluginContext pc) {		
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(pn);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}
}
