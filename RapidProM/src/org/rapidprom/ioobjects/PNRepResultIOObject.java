package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class PNRepResultIOObject extends AbstractRapidProMIOObject<PNRepResult> {

	private static final long serialVersionUID = -543887352437614848L;

	public PNRepResultIOObject(PNRepResult t, PluginContext context) {
		super(t, context);
	}

}
