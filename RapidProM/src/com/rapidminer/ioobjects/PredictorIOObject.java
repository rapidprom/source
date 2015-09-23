package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.prediction.Predictor;

public class PredictorIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private Predictor predictor = null;

	public PredictorIOObject (Predictor predictor) {
		this.predictor = predictor;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setPredictor(Predictor predictor) {
		this.predictor = predictor;
	}

	public Predictor getPredictor() {
		return predictor;
	}

	public String toResultString() {
		String extractName = predictor.toString();
		return "PredictorIOObject:" + extractName;
	}

	public Predictor getData() {
		return predictor;
	}

}
