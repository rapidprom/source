package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class PetriNetIOObject extends AbstractRapidProMIOObject<Petrinet> {

	private static final long serialVersionUID = -4574922526705299348L;

	public PetriNetIOObject(Petrinet t, PluginContext context) {
		super(t, context);
	}

}
