package org.rapidprom.ioobjects.abstr;

import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.ioobjects.RapidProMIOObject;

import com.rapidminer.operator.ResultObjectAdapter;

public abstract class AbstractRapidProMIOObject<T> extends ResultObjectAdapter
		implements RapidProMIOObject<T> {

	private static final long serialVersionUID = -7924883865640486269L;

	protected final T artifact;

	protected final PluginContext context;

	public PluginContext getPluginContext() {
		return context;
	}

	public AbstractRapidProMIOObject(final T t, final PluginContext context) {
		this.artifact = t;
		this.context = context;
	}

	public T getArtifact() {
		return artifact;
	}
}
