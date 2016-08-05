package org.rapidprom.ioobjects.streams.event;

import org.processmining.eventstream.core.interfaces.XSStaticXSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSStaticXSEventStreamIOObject
		extends AbstractRapidProMIOObject<XSStaticXSEventStream> {

	private static final long serialVersionUID = 3472668931720519972L;

	public XSStaticXSEventStreamIOObject(XSStaticXSEventStream t,
			PluginContext context) {
		super(t, context);
	}

}
