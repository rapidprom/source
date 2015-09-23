package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.fuzzymodel.anim.FuzzyAnimation;

public class FuzzyAnimationIOObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private FuzzyAnimation fuzzyAnimation = null;

	public FuzzyAnimationIOObject (FuzzyAnimation fuzzyAnimation) {
		this.fuzzyAnimation = fuzzyAnimation;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setFuzzyAnimation(FuzzyAnimation fuzzyAnimation) {
		this.fuzzyAnimation = fuzzyAnimation;
	}

	public FuzzyAnimation getFuzzyAnimation() {
		return fuzzyAnimation;
	}

	public String toResultString() {
		String extractName = fuzzyAnimation.toString();
		return "FuzzyAnimationIOObject:" + extractName;
	}

	public FuzzyAnimation getData() {
		return fuzzyAnimation;
	}

}
