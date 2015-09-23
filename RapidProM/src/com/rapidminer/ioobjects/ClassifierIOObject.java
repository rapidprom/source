package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.plugin.PluginContext;

public class ClassifierIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private XEventClassifier classifier = null;

	public ClassifierIOObject (XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public String toResultString() {
		String extractName = classifier.toString();
		return "ClassifierIOObject:" + extractName;
	}

	public XEventClassifier getData() {
		return classifier;
	}

}
