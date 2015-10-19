package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class MetricsRepositoryIOObject extends AbstractRapidProMIOObject<MetricsRepository>{

	private static final long serialVersionUID = 6352054321839117409L;

	public MetricsRepositoryIOObject(MetricsRepository t, PluginContext context) {
		super(t, context);
		
	}
}
