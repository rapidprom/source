package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.fuzzymodel.miner.ui.FastTransformerPanel;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.ManifestIOObject;
import com.rapidminer.ioobjects.MetricsRepositoryIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class MetricsRepositoryIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof MetricsRepositoryIOObject) {
			MetricsRepositoryIOObject object = (MetricsRepositoryIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof MetricsRepositoryIOObject) {
			
			MetricsRepositoryIOObject object = (MetricsRepositoryIOObject) renderable;
			// try to get the visualizer in ProM
			
			FastTransformerPanel promPanel = (FastTransformerPanel) runVisualization(object.getData(), object.getPluginContext());
			
			//add html tags to change the font size of the activities
			FuzzyGraph graph = promPanel.getGraph();
			graph.getAttributeMap().put(AttributeMap.AUTOSIZE, true);
			for(FMNode node : graph.getNodes())
			{
				String name = node.getLabel(); // to store the activity name
				String percentage; // to store the occurence percentage
				
				System.out.println(name);
				
				percentage = name.substring(name.lastIndexOf("<br>")+4, name.lastIndexOf("<html>"));
				if(name.contains("C_"))// if has the course code first of not
					name = name.substring(14,name.indexOf("<br>"));
				else
					name = name.substring(6,name.indexOf("<br>"));
				
//				node.setLabel("<html><font size = 5>" + name + "<br>" + percentage + "</font></html>");
				node.setLabel("<html><font size = 5>" + name + "</font></html>");  //no percentage variable (significance, NOT frequency)
			}
			
			
			ProMJGraphPanel panel = (ProMJGraphPanel) promPanel.getGraphPanel();
			
			ProMJGraph jgraph = Utilities.getSizedGraph(panel,desiredWidth,desiredHeight);
			
			panel.repaint();
			promPanel.redrawGraph(false);
			
			return new DefaultComponentRenderable(jgraph);
		}
		return new DefaultReadable("No visualization available.");
	}

	public static JComponent runVisualization(MetricsRepository metricsRepository, PluginContext pc) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(metricsRepository);
		
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

	public String getName() {
		return "MetricsRepositoryrenderer";
	}

}