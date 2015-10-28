package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSPublisher;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSPublisherIOObject
		extends AbstractRapidProMIOObject<XSPublisher> {

	private static final long serialVersionUID = 2262281232123628766L;

	public XSPublisherIOObject(XSPublisher x, PluginContext c) {
		super(x, c);
	}
}
