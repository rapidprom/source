package org.rapidprom.ioobjects;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class AcceptingPetriNetIOObject
		extends AbstractRapidProMIOObject<AcceptingPetriNet> {

	private static final long serialVersionUID = 7775793727750096919L;

	public AcceptingPetriNetIOObject(AcceptingPetriNet t,
			PluginContext context) {
		super(t, context);
	}

}
