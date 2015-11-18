package org.rapidprom.ioobjects.streams.event;

import org.processmining.eventstream.readers.processtree.XSEventStreamToProcessTreeReader;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;

public class XSEventStreamToProcessTreePetriNetReaderIOObject
		extends XSReaderIOObject<XSEventStreamToProcessTreeReader> {

	private static final long serialVersionUID = -7913307437313684794L;

	public XSEventStreamToProcessTreePetriNetReaderIOObject(
			XSEventStreamToProcessTreeReader reader, PluginContext context) {
		super(reader, context);
		// TODO Auto-generated constructor stub
	}

}
