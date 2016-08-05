package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class CPNModelIOObject extends
		AbstractRapidProMIOObject<ColouredPetriNet> {

	private static final long serialVersionUID = 4158861487079429809L;

	public CPNModelIOObject(ColouredPetriNet t, PluginContext context) {
		super(t, context);
	}

}