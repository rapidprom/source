package org.rapidprom.ioobjects.streams.event;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSReader;
import org.rapidprom.ioobjects.streams.XSReaderIOObject;

public class XSEventStreamToAcceptingPetriNetReaderIOObject
		extends XSReaderIOObject<XSEvent, AcceptingPetriNet> {

	private static final long serialVersionUID = -3049278931265566812L;

	public XSEventStreamToAcceptingPetriNetReaderIOObject(
			XSReader<XSEvent, AcceptingPetriNet> reader,
			PluginContext context) {
		super(reader, context);
	}

}
