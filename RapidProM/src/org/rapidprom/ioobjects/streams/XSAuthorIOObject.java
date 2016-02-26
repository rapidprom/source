package org.rapidprom.ioobjects.streams;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.stream.core.interfaces.XSAuthor;
import org.processmining.stream.core.interfaces.XSDataPacket;

public class XSAuthorIOObject<D extends XSDataPacket<?, ?>>
		extends XSWriterXSRunnableIOObject<XSAuthor<D>> {

	private static final long serialVersionUID = 36779985659303201L;

	public XSAuthorIOObject(XSAuthor<D> t, PluginContext context) {
		super(t, context);
	}

}
