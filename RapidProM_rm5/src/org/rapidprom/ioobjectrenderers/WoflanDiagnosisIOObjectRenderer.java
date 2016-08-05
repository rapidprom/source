package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.WoflanDiagnosisIOObject;

public class WoflanDiagnosisIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<WoflanDiagnosisIOObject> {

	public String getName() {
		return "WoflanDiagnosisrenderer";
	}

	@Override
	protected JComponent runVisualization(WoflanDiagnosisIOObject artifact) {
		JComponent panel = new JTextArea(artifact.getArtifact().toString());
		panel.setEnabled(false);
		return panel;
	}

}