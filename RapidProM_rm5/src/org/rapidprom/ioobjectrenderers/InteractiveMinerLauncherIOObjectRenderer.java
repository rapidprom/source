package org.rapidprom.ioobjectrenderers;

import javax.swing.JComponent;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMiner;
import org.rapidprom.ioobjectrenderers.abstr.AbstractRapidProMIOObjectRenderer;
import org.rapidprom.ioobjects.InteractiveMinerLauncherIOObject;

public class InteractiveMinerLauncherIOObjectRenderer extends
		AbstractRapidProMIOObjectRenderer<InteractiveMinerLauncherIOObject> {

	@Override
	public String getName() {
		return "Inductive Visual Miner renderer";
	}

	@Override
	protected JComponent runVisualization(
			InteractiveMinerLauncherIOObject artifact) {
		InductiveVisualMiner visualizer = new InductiveVisualMiner();
		return visualizer.visualise(artifact.getPluginContext(),
				artifact.getArtifact(), new ProMCancellerImpl());
	}
	
	private static final class ProMCancellerImpl implements ProMCanceller {

		private boolean isCancelled = false;

		public boolean isCancelled() {
			return isCancelled;
		}

		@SuppressWarnings("unused")
		public void cancel() {
			isCancelled = true;
		}

	}

}