package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSReader;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSReaderIOObject<T extends XSReader<? extends XSDataPacket<?, ?>, ?>>
		extends AbstractRapidProMIOObject<T> {

	private static final long serialVersionUID = -7862503192309811538L;

	public XSReaderIOObject(T reader, PluginContext context) {
		super(reader, context);
	}

}
