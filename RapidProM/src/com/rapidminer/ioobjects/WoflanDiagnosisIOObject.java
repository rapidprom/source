package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.WoflanDiagnosis;

public class WoflanDiagnosisIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private WoflanDiagnosis woflanDiagnosis = null;

	public WoflanDiagnosisIOObject (WoflanDiagnosis woflanDiagnosis) {
		this.woflanDiagnosis = woflanDiagnosis;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setWoflanDiagnosis(WoflanDiagnosis woflanDiagnosis) {
		this.woflanDiagnosis = woflanDiagnosis;
	}

	public WoflanDiagnosis getWoflanDiagnosis() {
		return woflanDiagnosis;
	}

	public String toResultString() {
		String extractName = woflanDiagnosis.toString();
		return "WoflanDiagnosisIOObject:" + extractName;
	}

	public WoflanDiagnosis getData() {
		return woflanDiagnosis;
	}

}
