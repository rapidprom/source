package org.rapidprom.ioobjects.streams;

import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSEventStreamIOObject
		extends AbstractRapidProMIOObject<XSEventStream> {

	private static final long serialVersionUID = -6518688117417296076L;

	public XSEventStreamIOObject(XSEventStream s, PluginContext c) {
		super(s, c);
	}
}
