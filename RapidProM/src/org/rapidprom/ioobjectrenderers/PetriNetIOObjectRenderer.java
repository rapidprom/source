package org.rapidprom.ioobjectrenderers;


import java.awt.Component;
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.rapidprom.ioobjects.PetriNetIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;


public class PetriNetIOObjectRenderer extends AbstractRenderer {

	@Override
	public String getName() {
		return "Petri net renderer";
	}

	@Override
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof PetriNetIOObject) {
			PetriNetIOObject object = (PetriNetIOObject) renderable;
			PetriNetVisualization visualizer = new PetriNetVisualization();
			return visualizer.visualize(object.getPluginContext(), object.getPn());
		}
		return null;
	}

	@Override
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		if (renderable instanceof PetriNetIOObject) {									
			return new DefaultComponentRenderable(getVisualizationComponent(renderable, ioContainer));
		}
		return new DefaultReadable("No Petri Net visualization available.");
	}
}
