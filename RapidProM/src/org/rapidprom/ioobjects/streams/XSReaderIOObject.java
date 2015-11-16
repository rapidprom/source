package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSReader;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSReaderIOObject<T1 extends XSDataPacket<?, ?>, T2>
		extends AbstractRapidProMIOObject<XSReader<T1, T2>> {

	private static final long serialVersionUID = -7862503192309811538L;

	public XSReaderIOObject(XSReader<T1, T2> t, PluginContext context) {
		super(t, context);
	}

}
