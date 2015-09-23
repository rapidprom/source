package com.rapidminer.ioobjectrenderers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner.InteractiveMinerLauncher;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.InteractiveMinerLauncherIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class InteractiveMinerLauncherIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof InteractiveMinerLauncherIOObject) {
			InteractiveMinerLauncherIOObject object = (InteractiveMinerLauncherIOObject)  renderable;
			JComponent panel = runVisualization(object.getInteractiveMinerLauncher(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultReadable("Not implemented yet.");
	}

	public static JComponent runVisualization(InteractiveMinerLauncher interactiveMinerLauncher, PluginContext pc) {
		
/*		
		CallProm tp = new CallProm();
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(interactiveMinerLauncher);
		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
		return runVisualizationPlugin;
*/
		
		InductiveVisualMiner iminer = new InductiveVisualMiner();
		return iminer.visualise(pc, interactiveMinerLauncher);
	}

	public String getName() {
		return "InteractiveMinerLauncherrenderer";
	}

}