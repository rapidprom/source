package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class SocialNetworkIOObject extends
		AbstractRapidProMIOObject<SocialNetwork> {

	private static final long serialVersionUID = 4434539088563859762L;

	public SocialNetworkIOObject(SocialNetwork t, PluginContext context) {
		super(t, context);
	}

}
