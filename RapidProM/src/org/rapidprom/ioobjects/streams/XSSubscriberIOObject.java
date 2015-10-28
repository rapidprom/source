package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSSubscriber;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSSubscriberIOObject
		extends AbstractRapidProMIOObject<XSSubscriber> {

	private static final long serialVersionUID = 526853673220495304L;

	public XSSubscriberIOObject(XSSubscriber x, PluginContext c) {
		super(x, c);
	}

}
