package org.rapidprom.ioobjects;

import org.processmining.csmminer.CSMMinerResults;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class CSMMinerResultIOObject extends AbstractRapidProMIOObject<CSMMinerResults> {

	private static final long serialVersionUID = 7794727486401629115L;

	public CSMMinerResultIOObject(CSMMinerResults t, PluginContext context) {
		super(t, context);
	}

}
