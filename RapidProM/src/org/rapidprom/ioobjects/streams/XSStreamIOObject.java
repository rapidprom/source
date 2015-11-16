package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.processmining.stream.core.interfaces.XSStream;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSStreamIOObject<T extends XSDataPacket<?, ?>>
		extends AbstractRapidProMIOObject<XSStream<T>> {

	public XSStreamIOObject(XSStream<T> t, PluginContext context) {
		super(t, context);
	}

	private static final long serialVersionUID = -9184396196126795035L;

}
