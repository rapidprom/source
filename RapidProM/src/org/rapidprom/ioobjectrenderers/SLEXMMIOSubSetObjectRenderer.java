package org.rapidprom.ioobjectrenderers;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Set;

import org.processmining.database.metamodel.dapoql.ui.components.DAPOQLResultsPanel;
import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.rapidprom.ioobjectrenderers.abstr.AbstractMultipleVisualizersRenderer;
import org.rapidprom.ioobjects.SLEXMMSubSetIOObject;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class SLEXMMIOSubSetObjectRenderer extends
		AbstractMultipleVisualizersRenderer<SLEXMMIOSubSetObjectVisualizationType> {

	public SLEXMMIOSubSetObjectRenderer() {
		super(EnumSet.allOf(SLEXMMIOSubSetObjectVisualizationType.class),
				"SLEXMM SubSet renderer");
	}

	private Component defaultComponent = null;
	private WeakReference<Set<?>> defaultMM = null;

	protected Component visualizeRendererOption(
			SLEXMMIOSubSetObjectVisualizationType e, Object renderable,
			IOContainer ioContainer) {

		System.out.println("looking for renderer!");
		Component result;
		switch (e) {
		default:
		case DEFAULT:
			result = createDefaultVisualizerComponent(renderable, ioContainer);
			break;
		}
		return result;
	}
	
	@SuppressWarnings("unused")
	protected Component createDefaultVisualizerComponent(Object renderable,
			IOContainer ioContainer) {
		SLEXMMSubSetIOObject soobject = (SLEXMMSubSetIOObject) renderable;
		SLEXMMStorageMetaModelImpl mm = soobject.getArtifact();
		
		if (defaultComponent == null || defaultMM == null
				|| !(mm.equals(defaultMM.get()))) {
			try {
				defaultComponent = new DAPOQLResultsPanel(mm,soobject.getType(),soobject.getResults());
				
				defaultMM = new WeakReference<Set<?>>(soobject.getResults());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultComponent;
	}

	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultComponentRenderable(
				getVisualizationComponent(renderable, ioContainer));
	}
}