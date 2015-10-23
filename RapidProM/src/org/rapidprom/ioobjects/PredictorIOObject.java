package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.prediction.Predictor;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class PredictorIOObject extends AbstractRapidProMIOObject<Predictor> {

	private static final long serialVersionUID = -1708411560288054840L;

	public PredictorIOObject(Predictor t, PluginContext context) {
		super(t, context);
	}
}
