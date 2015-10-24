package org.rapidprom.ioobjectrenderers;

import java.awt.Component;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.rapidprom.ioobjects.TransitionSystemIOObject;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class TransitionSystemIOObjectRenderer extends AbstractRenderer {
	@Override
	public Component getVisualizationComponent(Object renderable,
			IOContainer ioContainer) {
		if (renderable instanceof TransitionSystemIOObject) {
			try {
				TransitionSystemIOObject object = (TransitionSystemIOObject) renderable;
				JComponent panel = runVisualization(object.getArtifact(),
						object.getPluginContext());
				return panel;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof TransitionSystemIOObject) {
			TransitionSystemIOObject object = (TransitionSystemIOObject) renderable;
			return new DefaultComponentRenderable(runVisualization(
					object.getArtifact(), object.getPluginContext()));
		}
		return new DefaultReadable(
				"No Transition System visualization available.");
	}

	public static JComponent runVisualization(TSMinerTransitionSystem ts,
			PluginContext pc) {
		System.out.println("Is here!");
		return ProMJGraphVisualizer.instance().visualizeGraph(pc, ts);
	}

	public String getName() {
		return "TransitionSystemRenderer";
	}

}