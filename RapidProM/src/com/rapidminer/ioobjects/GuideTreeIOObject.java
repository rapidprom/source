package com.rapidminer.ioobjects;

import com.rapidminer.operator.ResultObjectAdapter;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.guidetreeminer.tree.GuideTree;

public class GuideTreeIOObject extends ResultObjectAdapter implements ProMIOObject {

	private static final long serialVersionUID = 1L;

	private PluginContext pc = null;
	private GuideTree guideTree = null;

	public GuideTreeIOObject (GuideTree guideTree) {
		this.guideTree = guideTree;
	}

	public void setPluginContext (PluginContext pc) {
		this.pc = pc;
	}

	public PluginContext getPluginContext () {
		return this.pc;
	}

	public void setGuideTree(GuideTree guideTree) {
		this.guideTree = guideTree;
	}

	public GuideTree getGuideTree() {
		return guideTree;
	}

	public String toResultString() {
		String extractName = guideTree.toString();
		return "GuideTreeIOObject:" + extractName;
	}

	public GuideTree getData() {
		return guideTree;
	}

	@Override
	public void clear() {
		this.pc = null;
		this.guideTree = null;		
	}

}
