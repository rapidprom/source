package org.rapidprom.ioobjectrenderers.abstr;

import java.awt.Component;

import javax.swing.JComponent;

import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public abstract class AbstractRapidProMIOObjectRenderer<T extends AbstractRapidProMIOObject<?>>
		extends AbstractRenderer {

	@SuppressWarnings("unchecked")
	@Override
	public Component getVisualizationComponent(Object renderable,
			IOContainer ioContainer) {
		if (renderable instanceof AbstractRapidProMIOObject<?>) {
			T object = (T) renderable;
			JComponent panel = runVisualization(object);
			return panel;
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultComponentRenderable(
				getVisualizationComponent(renderable, ioContainer));
	}

	protected abstract JComponent runVisualization(T ioObject);
}
