package org.rapidprom.ioobjectrenderers;

import java.awt.Component;
import java.awt.Dimension;
import java.lang.ref.WeakReference;
import java.util.EnumSet;

import javax.swing.JFrame;

import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.visualization.PNLogReplayResultVisPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;
import org.rapidprom.ioobjectrenderers.abstr.AbstractMultipleVisualizersRenderer;
import org.rapidprom.ioobjects.PNRepResultIOObject;

import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

import javassist.tools.rmi.ObjectNotFoundException;

public class PNRepResultIOObjectRenderer extends
		AbstractMultipleVisualizersRenderer<PNRepResultIOObjectVisualizationType> {

	public PNRepResultIOObjectRenderer() {
		super(EnumSet.allOf(PNRepResultIOObjectVisualizationType.class),
				"PNRepResult renderer");
	}

	private Component projectOnModelComponent = null;
	private WeakReference<PNRepResult> projectOnModelObject = null;
	private Component projectOnLogComponent = null;
	private WeakReference<PNRepResult> projectOnLogObject = null;

	@Override
	public String getName() {
		return "PNRepResult renderer";
	}

	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		Component panel = null;
		try {
			panel = createModelComponet(renderable, ioContainer);
			JFrame frame = new JFrame();
			frame.getContentPane().add(panel);
			frame.setSize(desiredWidth, desiredHeight);
			frame.setPreferredSize(new Dimension(desiredWidth, desiredHeight));
			frame.setMinimumSize(new Dimension(desiredWidth, desiredHeight));
			frame.setMaximumSize(new Dimension(desiredWidth, desiredHeight));
			frame.pack();
			frame.revalidate();
			frame.repaint();
			frame.setVisible(true);
			Thread.sleep(1000);
			frame.setVisible(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DefaultComponentRenderable(panel);
	}

	@Override
	protected Component visualizeRendererOption(
			PNRepResultIOObjectVisualizationType visualizerType,
			Object renderable, IOContainer ioContainer) {

		Component result = null;
		switch (visualizerType) {
		case PROJECT_ON_LOG:
			result = createLogComponet(renderable, ioContainer);
			break;
		default:
		case PROJECT_ON_MODEL:
			try {
				result = createModelComponet(renderable, ioContainer);
			} catch (ObjectNotFoundException e) {
				e.printStackTrace();
			}
			break;
		}
		return result;
	}

	protected Component createLogComponet(Object renderable,
			IOContainer ioContainer) {
		PNRepResultIOObject artifact = (PNRepResultIOObject) renderable;
		if (projectOnLogComponent == null || projectOnLogObject == null
				|| !(artifact.equals(projectOnLogObject.get()))) {
			projectOnLogComponent = new PNLogReplayResultVisPanel(
					artifact.getPn().getArtifact(), artifact.getXLog(),
					artifact.getArtifact(),
					artifact.getPluginContext().getProgress());
			projectOnLogObject = new WeakReference<PNRepResult>(
					artifact.getArtifact());
		}
		return projectOnLogComponent;
	}

	protected Component createModelComponet(Object renderable,
			IOContainer ioContainer) throws ObjectNotFoundException {
		PNRepResultIOObject artifact = (PNRepResultIOObject) renderable;
		if (projectOnModelComponent == null || projectOnModelObject == null
				|| !(artifact.equals(projectOnModelObject.get()))) {
			projectOnModelComponent = new PNLogReplayProjectedVisPanel(
					artifact.getPluginContext(), artifact.getPn().getArtifact(),
					artifact.getPn().getInitialMarking(), artifact.getXLog(),
					artifact.getMapping(), artifact.getArtifact());
			projectOnModelObject = new WeakReference<PNRepResult>(
					artifact.getArtifact());
		}
		return projectOnModelComponent;
	}

}