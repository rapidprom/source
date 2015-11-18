package org.rapidprom.ioobjects.streams.event;

import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;

public class XSEventStreamToAcceptingPetriNetReaderIOObject
		extends XSReaderIOObject<XSEventStreamToAcceptingPetriNetReader> {

	private static final long serialVersionUID = -3049278931265566812L;

	public XSEventStreamToAcceptingPetriNetReaderIOObject(
			XSEventStreamToAcceptingPetriNetReader reader,
			PluginContext context) {
		super(reader, context);
	}

}
