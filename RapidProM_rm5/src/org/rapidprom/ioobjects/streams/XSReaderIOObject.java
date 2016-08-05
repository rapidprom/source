package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSReader;

public class XSReaderIOObject<D extends XSDataPacket<?, ?>, R>
		extends XSWritableXSRunnableIOObject<XSReader<D, R>> {

	private static final long serialVersionUID = -7862503192309811538L;

	public XSReaderIOObject(XSReader<D, R> reader, PluginContext context) {
		super(reader, context);
	}

}
