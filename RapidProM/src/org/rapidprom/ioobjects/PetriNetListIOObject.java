package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.petrinets.list.PetriNetList;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class PetriNetListIOObject extends AbstractRapidProMIOObject<PetriNetList> {

	private static final long serialVersionUID = -9118010607628257933L;

	public PetriNetListIOObject(PetriNetList t, PluginContext context) {
		super(t, context);
	}

}
