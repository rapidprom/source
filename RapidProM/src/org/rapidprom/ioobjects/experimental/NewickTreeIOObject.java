package org.rapidprom.ioobjects.experimental;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.models.NewickTree;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class NewickTreeIOObject extends AbstractRapidProMIOObject<NewickTree> {

	private static final long serialVersionUID = -4536644614598642407L;

	public NewickTreeIOObject(NewickTree t, PluginContext context) {
		super(t, context);
	}
}
