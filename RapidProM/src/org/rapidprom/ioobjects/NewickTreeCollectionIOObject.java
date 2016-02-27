package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.ptandloggenerator.models.NewickTreeCollection;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class NewickTreeCollectionIOObject
		extends AbstractRapidProMIOObject<NewickTreeCollection> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3203624016449189451L;

	public NewickTreeCollectionIOObject(NewickTreeCollection t,
			PluginContext context) {
		super(t, context);

	}
}
