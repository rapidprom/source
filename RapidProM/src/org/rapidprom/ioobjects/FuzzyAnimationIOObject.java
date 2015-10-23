package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.fuzzymodel.anim.FuzzyAnimation;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class FuzzyAnimationIOObject extends
		AbstractRapidProMIOObject<FuzzyAnimation> {

	private static final long serialVersionUID = -582044080446972654L;

	public FuzzyAnimationIOObject(FuzzyAnimation t, PluginContext context) {
		super(t, context);
	}

}
