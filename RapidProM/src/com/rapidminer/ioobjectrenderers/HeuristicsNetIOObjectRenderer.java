package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.HeuristicsNetIOObject;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class HeuristicsNetIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof HeuristicsNetIOObject) {
			HeuristicsNetIOObject object = (HeuristicsNetIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof HeuristicsNetIOObject) {
			HeuristicsNetIOObject object = (HeuristicsNetIOObject) renderable;
			
			//ProMJGraphPanel promPanel = (ProMJGraphPanel) runVisualization(object.getData(), object.getPluginContext());
			//ProMJGraph graph = Utilities.getSizedGraph(promPanel,desiredWidth,desiredHeight);
		 
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			JSplitPane splitPane = (JSplitPane) panel.getComponent(0);
			JPanel component = (JPanel) splitPane.getComponent(1);
			
			return new DefaultComponentRenderable(Utilities.getSizedPanel(panel,component, desiredWidth, desiredHeight-50));
		}
		return new DefaultReadable("No Heuristics Net visualization available.");
	}

	public static JComponent runVisualization(HeuristicsNet heuristicsNet, PluginContext pc) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(heuristicsNet);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

	public String getName() {
		return "HeuristicsNetrenderer";
	}

}
