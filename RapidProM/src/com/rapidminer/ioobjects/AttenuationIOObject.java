package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;

public class AttenuationIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private Attenuation attenuation = null;

	public AttenuationIOObject (Attenuation attenuation) {
		this.attenuation = attenuation;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setAttenuation(Attenuation attenuation) {
		this.attenuation = attenuation;
	}

	public Attenuation getAttenuation() {
		return attenuation;
	}

	public String toResultString() {
		String extractName = attenuation.toString();
		return "AttenuationIOObject:" + extractName;
	}

	public Attenuation getData() {
		return attenuation;
	}

}
