package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class TransitionSystemIOObject extends AbstractRapidProMIOObject<TSMinerTransitionSystem> {


	private static final long serialVersionUID = 7513635369374245933L;

	public TransitionSystemIOObject(TSMinerTransitionSystem t,
			PluginContext context) {
		super(t, context);
	}

	
}
