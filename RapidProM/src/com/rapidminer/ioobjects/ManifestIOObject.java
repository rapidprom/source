package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;

public class ManifestIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private Manifest manifest = null;
	
	private String visType = "";

	public ManifestIOObject (Manifest manifest) {
		this.manifest = manifest;
	}

	public void setPluginContext (PluginContext pluginContext) {
		this.pc = pluginContext;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setManifest(Manifest manifest) {
		this.manifest = manifest;
	}

	public Manifest getManifest() {
		return manifest;
	}

	public String toResultString() {
		String extractName = manifest.toString();
		return "ManifestIOObject:" + extractName;
	}

	public Manifest getData() {
		return manifest;
	}
	
	public void setVisType (String visType) {
		this.visType = visType;
	}

	public String getVisType() {
		return this.visType;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.manifest = null;		
	}

}
