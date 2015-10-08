package org.rapidprom.ioobjectrenderers;

import java.awt.Component;

import javax.swing.JComponent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.plugins.heuristicsnet.visualizer.HeuristicsNetAnnotatedVisualization;
import org.rapidprom.ioobjects.HeuristicsNetIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

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
			return new DefaultComponentRenderable(runVisualization(object.getData(), object.getPluginContext()));
		}
		return new DefaultReadable("No Heuristics Net visualization available.");
	}

	public static JComponent runVisualization(HeuristicsNet heuristicsNet, PluginContext pc) {		
		return HeuristicsNetAnnotatedVisualization.visualize(pc, heuristicsNet);
	}

	public String getName() {
		return "HeuristicsNetrenderer";
	}

}
