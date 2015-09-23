package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.MarkingIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class MarkingIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof MarkingIOObject) {
			return new JLabel("This object is not visualizable:" + getName());
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultReadable("This object is not visualizable:" + getName());
	}

	public String getName() {
		return "Marking";
	}

}
