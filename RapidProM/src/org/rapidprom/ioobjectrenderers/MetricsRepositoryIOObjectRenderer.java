package org.rapidprom.ioobjectrenderers;

import java.awt.Component;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.plugins.fuzzymodel.FastTransformerVisualization;
import org.rapidprom.ioobjects.MetricsRepositoryIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class MetricsRepositoryIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof MetricsRepositoryIOObject) {
			MetricsRepositoryIOObject object = (MetricsRepositoryIOObject) renderable;
			JComponent panel = runVisualization(object.getData(),
					object.getPluginContext());
			return panel;
		}
		return null;
	}

	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof MetricsRepositoryIOObject) {
			return new DefaultComponentRenderable(
					runVisualization(((MetricsRepositoryIOObject) renderable)
							.getMetricsRepository(),
							((MetricsRepositoryIOObject) renderable)
									.getPluginContext()));
		}
		return new DefaultReadable("No visualization available.");
	}

	public static JComponent runVisualization(
			MetricsRepository metricsRepository, PluginContext pc) {
		FastTransformerVisualization visualizer = new FastTransformerVisualization();
		return visualizer.visualize(pc, metricsRepository);
	}

	public String getName() {
		return "MetricsRepositoryrenderer";
	}

}