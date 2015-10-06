package org.rapidprom.ioobjectrenderers;


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
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.prom.CallProm;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;
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
			PetriNetVisualization visualizer = new PetriNetVisualization();
			return visualizer.visualize(object.getPluginContext(), object.getPn());
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof PetriNetIOObject) {									
			return new DefaultComponentRenderable(getVisualizationComponent(renderable, ioContainer));
		}
		return new DefaultReadable("No Petri Net visualization available.");
	}
}
