//package com.rapidminer.ioobjectrenderers;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.awt.Component;
//
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JSplitPane;
//import javax.swing.JTabbedPane;
//import javax.swing.JViewport;
//
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;
//
//import com.rapidminer.callprom.CallProm;
//import com.rapidminer.ioobjects.DottedChartModelIOObject;
//import com.rapidminer.gui.renderer.AbstractRenderer;
//import com.rapidminer.gui.renderer.DefaultComponentRenderable;
//import com.rapidminer.gui.renderer.DefaultReadable;
//import com.rapidminer.operator.IOContainer;
//import com.rapidminer.report.Reportable;
//import com.rapidminer.util.Utilities;
//
//public class DottedChartModelIOObjectRenderer extends AbstractRenderer {
//
//	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
//		if (renderable instanceof DottedChartModelIOObject) {
//			DottedChartModelIOObject object = (DottedChartModelIOObject)  renderable;
//			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
//			return panel;
//		}
//		return null;
//	}
//	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
//		if (renderable instanceof DottedChartModelIOObject) {
//			DottedChartModelIOObject object = (DottedChartModelIOObject)  renderable;
//			// try to get the visualizer in ProM
//			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
//			JSplitPane splitPane = (JSplitPane) panel.getComponent(0);
//			JPanel panel0 = (JPanel) splitPane.getComponent(1);
//			JTabbedPane tabbedPane = (JTabbedPane) panel0.getComponent(0);
//			JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponent(0);
//			JViewport viewPort = (JViewport) scrollPane.getComponent(0);
//			JPanel component = (JPanel) viewPort.getComponent(0);
//			// put the thing in its own panel
//			return new DefaultComponentRenderable(Utilities.getSizedPanel(panel,component, desiredWidth, desiredHeight-50));
//		}
//
//		return new DefaultReadable("Not implemented yet.");
//	}
//
//	public static JComponent runVisualization(DottedChartModel dottedChartModel, PluginContext pc) {
//		CallProm tp = new CallProm();
//		List<Object> parameters = new ArrayList<Object>();
//		parameters.add(dottedChartModel);
//		JComponent runVisualizationPlugin = tp.runVisualizationPlugin(pc,"x",parameters);
//		return runVisualizationPlugin;
//	}
//
//	public String getName() {
//		return "DottedChartModelrenderer";
//	}
//
//}
