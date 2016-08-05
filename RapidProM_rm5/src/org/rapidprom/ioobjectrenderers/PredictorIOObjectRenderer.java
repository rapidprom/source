package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.prediction.PredictorVisualizer;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.PredictorIOObject;

public class PredictorIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<PredictorIOObject> {

	@Override
	public String getName() {
		return "Predictor renderer";
	}

	@Override
	protected JComponent runVisualization(PredictorIOObject artifact) {
		PredictorVisualizer visualizer = new PredictorVisualizer();
		JComponent result = null;
		try {
			result = visualizer.visualizePrediction(artifact.getPluginContext(),
					artifact.getArtifact());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}