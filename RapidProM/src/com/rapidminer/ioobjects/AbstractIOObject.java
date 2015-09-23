package com.rapidminer.ioobjects;

import org.processmining.framework.plugin.PluginContext;

import com.rapidminer.operator.ResultObjectAdapter;

public abstract class AbstractIOObject<T> extends ResultObjectAdapter implements
		ProMIOObject {

	private static final long serialVersionUID = -7924883865640486269L;
	protected T artifact = null;
	// needed for rendering
	private PluginContext pc = null;

	public AbstractIOObject(T t) {
		this.artifact = t;
	}

	public T getArtifact() {
		return artifact;
	}

	public void setArtifact(T t) {
		this.artifact = t;
	}

	public void setPluginContext(PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext() {
		return this.pc;
	}

	@Override
	public String toResultString() {
		String extractName = artifact.toString();
		return "ProMContextIOObject: " + extractName;
	}

	// use getArtifact()
	@Deprecated
	public T getData() {
		return this.artifact;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.artifact = null;
	}

}
