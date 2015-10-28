package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.plugins.socialnetwork.analysis.SocialNetworkAnalysisPlugin;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.SocialNetworkIOObject;

public class SocialNetworkIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<SocialNetworkIOObject> {

	@Override
	public String getName() {
		return "SocialNetwork renderer";
	}

	@Override
	protected JComponent runVisualization(SocialNetworkIOObject artifact) {
		return SocialNetworkAnalysisPlugin.invokeSNA(
				artifact.getPluginContext(), artifact.getArtifact());
	}

}
