package org.rapidprom.ioobjectrenderers;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.videolectureanalysis.renderer.SequentialProcessRenderer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.DotPanelIOObject;

import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class DotPanelIOObjectRenderer
		extends AbstractRapidProMIOObjectRenderer<DotPanelIOObject> {

	@Override
	public String getName() {
		return "DotPanel renderer";
	}

	@Override
	protected JComponent runVisualization(DotPanelIOObject ioObject) {
		return SequentialProcessRenderer.visualize(ioObject.getPluginContext(),
				ioObject.getArtifact());
	}
	
	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		
		DotPanel dotPanel = ((DotPanelIOObject) renderable).getArtifact();
		JFrame frame = new JFrame();
		frame.getContentPane().add(dotPanel);
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
		return new DefaultComponentRenderable(
				getVisualizationComponent(renderable, ioContainer));
	}

}
