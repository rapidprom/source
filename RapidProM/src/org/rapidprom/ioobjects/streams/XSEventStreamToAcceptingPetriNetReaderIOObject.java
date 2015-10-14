package org.rapidprom.ioobjects.streams;

import org.processmining.eventstream.readers.acceptingpetrinet.XSEventStreamToAcceptingPetriNetReader;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSEventStreamToAcceptingPetriNetReaderIOObject extends
		AbstractRapidProMIOObject<XSEventStreamToAcceptingPetriNetReader> {

	private static final long serialVersionUID = -8714440475950859133L;

	public XSEventStreamToAcceptingPetriNetReaderIOObject(
			XSEventStreamToAcceptingPetriNetReader t, PluginContext context) {
		super(t, context);
	}

}
