package org.rapidprom.ioobjects.streams.event;

import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;
import org.processmining.stream.core.interfaces.XSReader;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;

public class XSEventStreamToProcessTreePetriNetReaderIOObject
		extends XSReaderIOObject<XSEvent, ProcessTree> {

	private static final long serialVersionUID = -7913307437313684794L;

	public XSEventStreamToProcessTreePetriNetReaderIOObject(
			XSReader<XSEvent, ProcessTree> reader, PluginContext context) {
		super(reader, context);
	}

}
