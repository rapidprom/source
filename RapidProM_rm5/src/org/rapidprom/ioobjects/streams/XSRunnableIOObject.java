package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSRunnable;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSRunnableIOObject<T extends XSRunnable>
		extends AbstractRapidProMIOObject<T> {

	private static final long serialVersionUID = -173963109922143034L;

	public XSRunnableIOObject(T t, PluginContext context) {
		super(t, context);
	}

}
