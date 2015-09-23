package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.HeuristicsNetIOObject;
import com.rapidminer.ioobjects.TSMinerTransitionSystemIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class TSMinerTransitionSystemIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof TSMinerTransitionSystemIOObject) {
			TSMinerTransitionSystemIOObject object = (TSMinerTransitionSystemIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof TSMinerTransitionSystemIOObject) {
			TSMinerTransitionSystemIOObject object = (TSMinerTransitionSystemIOObject) renderable;
			ProMJGraphPanel promPanel = (ProMJGraphPanel) runVisualization(object.getData(), object.getPluginContext());
			ProMJGraph graph = Utilities.getSizedGraph(promPanel,desiredWidth,desiredHeight);
			return new DefaultComponentRenderable(graph);
		}
		return new DefaultReadable("No Transition System visualization available.");
	}

	public static JComponent runVisualization(TSMinerTransitionSystem tSMinerTransitionSystem, PluginContext pc) {
		
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(tSMinerTransitionSystem);
		JComponent runVisualizationPlugin;
		try
		{
			runVisualizationPlugin = tp.runVisualizationPlugin(pc,"xsd",parameters);
		}
		catch(Exception e)
		{
			runVisualizationPlugin = new JLabel("There was an capacity overload when displaying this Transition System, try again with a smaller one");
		}
		return runVisualizationPlugin;
	}

	public String getName() {
		return "TSMinerTransitionSystemrenderer";
	}

}