package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSAuthor;
import org.processmining.stream.core.interfaces.XSDataPacket;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class XSAuthorIOObject<T extends XSDataPacket<?, ?>>
		extends AbstractRapidProMIOObject<XSAuthor<T>> {

	private static final long serialVersionUID = 36779985659303201L;

	public XSAuthorIOObject(XSAuthor<T> t, PluginContext context) {
		super(t, context);
	}

}
