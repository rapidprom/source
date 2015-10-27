package org.rapidprom.ioobjects;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class PNRepResultIOObject extends AbstractRapidProMIOObject<PNRepResult> {

	private static final long serialVersionUID = -543887352437614848L;
	
	private Petrinet pn;
	private XLog log;

	public Petrinet getPn() {
		return pn;
	}

	public XLog getXLog() {
		return log;
	}
	
	public PNRepResultIOObject(PNRepResult t, PluginContext context, Petrinet pn, XLog log) {
		super(t, context);
		this.pn = pn;
		this.log = log;
	}

}
