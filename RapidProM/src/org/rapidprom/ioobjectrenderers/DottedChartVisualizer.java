package org.rapidprom.ioobjectrenderers;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.dottedchartanalysis.DottedChartAnalysis;
import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class DottedChartVisualizer
		extends AbstractRapidProMIOObjectRenderer<XLogIOObject> {

	@Override
	public String getName() {
		return "Dotted Chart log visualizer";
	}

	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		XLog xLog = ((XLogIOObject) renderable).getArtifact();

		PluginContext pluginContext = ProMPluginContextManager.instance()
				.getContext();

		DottedChartModel result = new DottedChartModel(pluginContext, xLog);
		JComponent panel = new DottedChartAnalysis(pluginContext, result);
		JSplitPane splitPane = (JSplitPane) panel.getComponent(0);
		JPanel panel0 = (JPanel) splitPane.getComponent(1);
		JTabbedPane tabbedPane = (JTabbedPane) panel0.getComponent(0);
		JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponent(0);
		JViewport viewPort = (JViewport) scrollPane.getComponent(0);
		JPanel component = (JPanel) viewPort.getComponent(0);

		JFrame frame = new JFrame();
		frame.getContentPane().add(component);
		frame.setSize(desiredWidth, desiredHeight);
		frame.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMinimumSize(new Dimension(desiredWidth, desiredHeight));
		frame.setMaximumSize(new Dimension(desiredWidth, desiredHeight));
		frame.pack();
		frame.revalidate();
		frame.repaint();
		frame.setVisible(true);
		// put a JDialog for indicating when done.
		// JOptionPane.showMessageDialog(null, "Click on OK button when done
		// with laying out the Dotted Chart.");
		// sleep for a while
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close the frame
		// frame.dispatchEvent(new WindowEvent(frame,
		// WindowEvent.WINDOW_CLOSING));
		return new DefaultComponentRenderable(component);

	}

	@Override
	protected JComponent runVisualization(XLogIOObject ioObject) {
		return null;

	}

}
