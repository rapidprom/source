package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class ManifestIOObject extends AbstractRapidProMIOObject<Manifest> {

	private static final long serialVersionUID = -4626719613934848329L;

	public ManifestIOObject(Manifest t, PluginContext context) {
		super(t, context);
	}

}
