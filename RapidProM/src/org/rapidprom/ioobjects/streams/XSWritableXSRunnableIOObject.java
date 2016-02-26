package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSRunnable;
import org.processmining.stream.core.interfaces.XSWritable;

public class XSWritableXSRunnableIOObject<T extends XSWritable<? extends XSDataPacket<?, ?>> & XSRunnable>
		extends XSRunnableIOObject<T> {

	private static final long serialVersionUID = -8550382465247394570L;

	public XSWritableXSRunnableIOObject(T t, PluginContext context) {
		super(t, context);
	}

}
