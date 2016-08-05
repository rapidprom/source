package org.rapidprom.ioobjects;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.rapidprom.ioobjectrenderers.PNRepResultIOObjectVisualizationType;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class PNRepResultIOObject extends AbstractRapidProMIOObject<PNRepResult> {

	private static final long serialVersionUID = -543887352437614848L;

	private PNRepResultIOObjectVisualizationType visType;
	private PetriNetIOObject pn;
	private XLog log;
	private TransEvClassMapping mapping;

	public PetriNetIOObject getPn() {
		return pn;
	}

	public XLog getXLog() {
		return log;
	}

	public TransEvClassMapping getMapping() {
		return mapping;
	}

	public void setVisualizationType(
			PNRepResultIOObjectVisualizationType visType) {
		this.visType = visType;

	}

	public PNRepResultIOObjectVisualizationType getVisualizationType() {
		return visType;
	}

	public PNRepResultIOObject(PNRepResult t, PluginContext context,
			PetriNetIOObject pn, XLog log, TransEvClassMapping mapping) {
		super(t, context);
		this.pn = pn;
		this.log = log;
		this.mapping = mapping;
	}

}
