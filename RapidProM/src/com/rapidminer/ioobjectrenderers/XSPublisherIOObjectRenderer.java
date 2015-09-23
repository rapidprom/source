package com.rapidminer.ioobjectrenderers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.processmining.stream.core.interfaces.XSPublisher;
import org.processmining.stream.visualizers.XSStreamPublisherVisualizer;
import org.rapidprom.prom.CallProm;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.ioobjects.XSPublisherIOObject;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class XSPublisherIOObjectRenderer extends AbstractRenderer {

	@Override
	public String getName() {
		return "XSPublisher renderer";
	}

	@Override
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof XSPublisherIOObject) {
			XSPublisherIOObject object = (XSPublisherIOObject) renderable;
			// try to get the visualizer in ProM
			JComponent panel = runVisualization(object.getArtifact(), object.getPluginContext());
			return panel;
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof XSPublisherIOObject) {
			XSPublisherIOObject object = (XSPublisherIOObject) renderable;
			
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			// put the thing in its own panel
			return new DefaultComponentRenderable(Utilities.getSizedPanel(panel,panel, desiredWidth, desiredHeight-50));
		}
		return new DefaultReadable("No XSPublisher visualization available.");
	}
	
	public static JComponent runVisualization(XSPublisher pn, PluginContext pc) {		
//		CallProm tp = new CallProm();
//		List<Object> parameters = new ArrayList<Object>();
//		parameters.add(pn);
//		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		
		return XSStreamPublisherVisualizer.visualizePublisher(pn);
	}

}
