package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner.InteractiveMinerLauncher;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class InteractiveMinerLauncherIOObject extends
		AbstractRapidProMIOObject<InteractiveMinerLauncher> {

	private static final long serialVersionUID = 869496066868352283L;

	public InteractiveMinerLauncherIOObject(InteractiveMinerLauncher t,
			PluginContext context) {
		super(t, context);
	}

}
