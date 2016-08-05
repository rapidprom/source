package org.rapidprom.ioobjects;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class AcceptingPetriNetArrayIOObject
		extends AbstractRapidProMIOObject<AcceptingPetriNetArray> {

	private static final long serialVersionUID = 3428618552462283666L;

	public AcceptingPetriNetArrayIOObject(AcceptingPetriNetArray t,
			PluginContext context) {
		super(t, context);
	}

}
