package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.models.NewickTree;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class NewickTreeIOObject
		extends AbstractRapidProMIOObject<NewickTree> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3203624016449189451L;

	public NewickTreeIOObject(NewickTree t,
			PluginContext context) {
		super(t, context);

	}
}
