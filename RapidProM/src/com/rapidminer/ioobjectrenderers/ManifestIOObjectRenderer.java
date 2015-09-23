package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.replayresult.visualization.PNLogReplayResultVisPanel;
//import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;
//import org.processmining.plugins.pnalignanalysis.visualization.projection.ProjectionVisPanel;
//import org.processmining.plugins.pnalignanalysis.visualization.projection.ViewPanel;
//import org.processmining.plugins.pnalignanalysis.visualization.projection.ZoomPanel;



import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.ManifestIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class ManifestIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof ManifestIOObject) {
			ManifestIOObject object = (ManifestIOObject)  renderable;
			JComponent panel = (JComponent) runVisualization(object.getData(), object.getPluginContext(),object.getVisType());
			return panel;
		}
		return null;
	}
	
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof ManifestIOObject) {
			ManifestIOObject object = (ManifestIOObject) renderable;
			// try to get the visualizer in ProM
			JComponent promPanel = (JComponent) runVisualization(object.getData(), object.getPluginContext(),object.getVisType());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return new DefaultComponentRenderable(Utilities.getSizedPanel(promPanel, promPanel, desiredWidth, desiredHeight));
		}
		return new DefaultReadable("No visualization available.");
	}
	
	public static JComponent runVisualization(Manifest manifest, PluginContext pc, String visType) {
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(manifest);
		if (visType.equals("Performance Projection to Model")) {
			Object[] objs = tp.runPlugin(pc, "x", "Performance Projection to Model", parameters);
			return (JComponent) objs[0];
		}
		else if (visType.equals("Project Manifest to Log")) {
			Object[] objs = tp.runPlugin(pc, "x", "Project Manifest to Log", parameters);
			return (JComponent) objs[0];
		}
		else {
			Object[] objs = tp.runPlugin(pc, "x", "Project Manifest to Model for Conformance", parameters);
			
			PNLogReplayProjectedVisPanel finalpanel = (PNLogReplayProjectedVisPanel) objs[0];
			//return (JComponent) finalpanel.getViewport().getView();
			JScrollPane js = new JScrollPane(finalpanel.getViewport());
			return (JComponent) js;
		}
		
	}
	
	public String getName() {
		return "Manifestrenderer";
	}

}
