package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;

public class SocialNetworkIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private SocialNetwork socialNetwork = null;

	public SocialNetworkIOObject (SocialNetwork socialNetwork) {
		this.socialNetwork = socialNetwork;
	}

	public void setPluginContext (PluginContext pluginContext) {
		this.pc = pluginContext;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setSocialNetwork(SocialNetwork socialNetwork) {
		this.socialNetwork = socialNetwork;
	}

	public SocialNetwork getSocialNetwork() {
		return socialNetwork;
	}

	public String toResultString() {
		String extractName = socialNetwork.toString();
		return "SocialNetworkIOObject:" + extractName;
	}

	public SocialNetwork getData() {
		return socialNetwork;
	}

}
