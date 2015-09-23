//package com.rapidminer.ioobjectrenderers;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.awt.Component;
//
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
//
//import com.rapidminer.callprom.CallProm;
//import com.rapidminer.ioobjects.DottedChartModelIOObject;
//import com.rapidminer.ioobjects.SocialNetworkIOObject;
//import com.rapidminer.gui.renderer.AbstractRenderer;
//import com.rapidminer.gui.renderer.DefaultComponentRenderable;
//import com.rapidminer.gui.renderer.DefaultReadable;
//import com.rapidminer.operator.IOContainer;
//import com.rapidminer.report.Reportable;
//import com.rapidminer.util.Utilities;
//
//public class SocialNetworkIOObjectRenderer extends AbstractRenderer {
//
//	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
//		if (renderable instanceof SocialNetworkIOObject) {
//			SocialNetworkIOObject object = (SocialNetworkIOObject)  renderable;
//			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
//			return panel;
//		}
//		return null;
//	}
//	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
//		if (renderable instanceof SocialNetworkIOObject) {
//			SocialNetworkIOObject object = (SocialNetworkIOObject) renderable;
//			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
//			Component viewer = panel.getComponent(0);
//			Component sizedPanel = Utilities.getSizedPanel(panel,viewer, desiredWidth, desiredHeight);
//			return new DefaultComponentRenderable(sizedPanel);
//		}
//		return null;
//	}
//
//	public static JComponent runVisualization(SocialNetwork socialNetwork, PluginContext pc) {
//		CallProm tp = new CallProm();
//		List<Object> parameters = new ArrayList<Object>();
//		parameters.add(socialNetwork);
//		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
//		return runVisualizationPlugin;
//	}
//
//	public String getName() {
//		return "SocialNetworkrenderer";
//	}
//
//}
