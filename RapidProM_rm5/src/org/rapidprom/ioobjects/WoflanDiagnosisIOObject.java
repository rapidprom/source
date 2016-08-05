package org.rapidprom.ioobjects;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.behavioralanalysis.woflan.WoflanDiagnosis;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class WoflanDiagnosisIOObject extends
		AbstractRapidProMIOObject<WoflanDiagnosis> {

	private static final long serialVersionUID = -3834518107921166815L;

	public WoflanDiagnosisIOObject(WoflanDiagnosis t, PluginContext context) {
		super(t, context);
	}

}
