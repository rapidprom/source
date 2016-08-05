package org.rapidprom.operators.ports.metadata;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.ports.metadata.MetaData;

public class XLogIOObjectMetaData extends MetaData {

	private static final long serialVersionUID = 3447751295083897459L;

	private List<XEventClassifier> classifiers;

	public XLogIOObjectMetaData() {
		super(XLogIOObject.class);
		classifiers = new ArrayList<XEventClassifier>();
	}

	@Deprecated
	public XLogIOObjectMetaData(XLog log) {
		super(XLogIOObject.class);
		classifiers = log.getClassifiers();
	}

	public XLogIOObjectMetaData(List<XEventClassifier> classifiers) {
		super(XLogIOObject.class);
		this.classifiers = classifiers;
	}

	public List<XEventClassifier> getXEventClassifiers() {
		return classifiers;
	}

	public void setXEventClassifiers(List<XEventClassifier> classifiers) {
		this.classifiers = classifiers;
	}

	@Override
	public MetaData clone() {
		XLogIOObjectMetaData clone = (XLogIOObjectMetaData) super.clone();
		clone.setXEventClassifiers(new ArrayList<>(getXEventClassifiers()));
		return clone;
	}
}
