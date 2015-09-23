package com.rapidminer.ioobjectrenderers;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.WoflanDiagnosis;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.WoflanDiagnosisIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultReadable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class WoflanDiagnosisIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof WoflanDiagnosisIOObject) {
			WoflanDiagnosisIOObject object = (WoflanDiagnosisIOObject)  renderable;
			JComponent panel = runVisualization(object.getData(), object.getPluginContext());
			return panel;
		}
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultReadable("Not implemented yet.");
	}

	public static JComponent runVisualization(WoflanDiagnosis woflanDiagnosis, PluginContext pc) {
		CallProm tp = new CallProm();
		JComponent panel = new JTextArea(woflanDiagnosis.toString());
		panel.setEnabled(false);
		return panel;
	}

	public String getName() {
		return "WoflanDiagnosisrenderer";
	}

}