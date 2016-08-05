package org.rapidprom.ioobjects.streams.event;

import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSEventStreamIOObject
		extends AbstractRapidProMIOObject<XSEventStream> {

	public XSEventStreamIOObject(XSEventStream t, PluginContext context) {
		super(t, context);
	}

	private static final long serialVersionUID = -6518688117417296076L;

}
