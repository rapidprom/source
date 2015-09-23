package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;

public class MetricsRepositoryIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private MetricsRepository metricsRepository = null;

	public MetricsRepositoryIOObject (MetricsRepository metricsRepository) {
		this.metricsRepository = metricsRepository;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setMetricsRepository(MetricsRepository metricsRepository) {
		this.metricsRepository = metricsRepository;
	}

	public MetricsRepository getMetricsRepository() {
		return metricsRepository;
	}

	public String toResultString() {
		String extractName = metricsRepository.toString();
		return "MetricsRepositoryIOObject:" + extractName;
	}

	public MetricsRepository getData() {
		return metricsRepository;
	}

}
