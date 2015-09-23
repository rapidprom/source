package com.rapidminer.ioobjectrenderers;


import java.util.ArrayList;
import java.util.List;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.interaction.ZoomInteractionPanel;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.rapidprom.prom.CallProm;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.fluxicon.slickerbox.components.*;


public class PetriNetIOObjectRenderer extends AbstractRenderer {

	@Override
	public String getName() {
		return "Petri net renderer";
	}

	@Override
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof PetriNetIOObject) {
			PetriNetIOObject object = (PetriNetIOObject) renderable;
			// try to get the visualizer in ProM
			JComponent panel = runVisualization(object.getPn(), object.getPluginContext());
			return panel;
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof PetriNetIOObject) {
			PetriNetIOObject object = (PetriNetIOObject) renderable;
			
			ProMJGraphPanel promPanel = (ProMJGraphPanel) runVisualization(object.getPn(), object.getPluginContext());
			ProMJGraph graph = Utilities.getSizedGraph(promPanel,desiredWidth,desiredHeight);
			//return new DefaultComponentRenderable(runVisualization(object.getPn(), object.getPluginContext()));
			return new DefaultComponentRenderable(graph);
		}
		return new DefaultReadable("No Petri Net visualization available.");
	}
	
	public static JComponent runVisualization(Petrinet pn, PluginContext pc) {		
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(pn);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
	}

}
