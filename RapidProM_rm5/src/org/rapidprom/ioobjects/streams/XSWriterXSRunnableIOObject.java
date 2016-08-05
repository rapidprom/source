package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSRunnable;
import org.processmining.stream.core.interfaces.XSWriter;

public class XSWriterXSRunnableIOObject<T extends XSWriter<? extends XSDataPacket<?, ?>> & XSRunnable>
		extends XSRunnableIOObject<T> {

	private static final long serialVersionUID = 2264908736826786732L;

	public XSWriterXSRunnableIOObject(T t, PluginContext context) {
		super(t, context);
	}

}
