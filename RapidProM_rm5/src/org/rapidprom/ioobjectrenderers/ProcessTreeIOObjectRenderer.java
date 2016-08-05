package org.rapidprom.ioobjectrenderers;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.EnumSet;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.inductiveVisualMiner.plugins.ProcessTreeVisualisationPlugin;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.visualization.tree.TreeVisualization;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.abstr.AbstractMultipleVisualizersRenderer;
import org.rapidprom.ioobjects.ProcessTreeIOObject;

import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;

public class ProcessTreeIOObjectRenderer
		extends
		AbstractMultipleVisualizersRenderer<ProcessTreeIOObjectVisualizationType> {

	public ProcessTreeIOObjectRenderer() {
		super(EnumSet.allOf(ProcessTreeIOObjectVisualizationType.class),
				"Process Tree renderer");
	}

	private Component defaultComponent = null;
	private WeakReference<ProcessTree> defaultProcessTree = null;
	private Component dotComponent = null;
	private WeakReference<ProcessTree> dotProcessTree = null;

	protected Component visualizeRendererOption(
			ProcessTreeIOObjectVisualizationType e, Object renderable,
			IOContainer ioContainer) {
		Component result;
		switch (e) {
		case DEFAULT:
			result = createDefaultComponet(renderable, ioContainer);
			break;
		default:
		case DOT:
			result = createDotComponent(renderable, ioContainer);
			break;
		}
		return result;
	}

	public Component createDefaultComponet(Object renderable,
			IOContainer ioContainer) {
		ProcessTreeIOObject object = (ProcessTreeIOObject) renderable;
		ProcessTree pt = object.getArtifact();
		if (defaultComponent == null || defaultProcessTree == null
				|| !(pt.equals(defaultProcessTree.get()))) {
			PluginContext pluginContext = ProMPluginContextManager.instance()
					.getContext();
			TreeVisualization visualizer = new TreeVisualization();
			defaultComponent = visualizer.visualize(pluginContext, pt);
			defaultProcessTree = new WeakReference<ProcessTree>(pt);
		}
		return defaultComponent;
	}

	public Component createDotComponent(Object renderable,
			IOContainer ioContainer) {
		ProcessTreeIOObject object = (ProcessTreeIOObject) renderable;
		ProcessTree pt = object.getArtifact();
		if (dotComponent == null || dotProcessTree == null
				|| !(pt.equals(dotProcessTree.get()))) {
			try {
				PluginContext pluginContext = ProMPluginContextManager
						.instance().getContext();
				ProcessTreeVisualisationPlugin visualizer = new ProcessTreeVisualisationPlugin();
				dotComponent = visualizer.fancy(pluginContext, pt);
				dotProcessTree = new WeakReference<ProcessTree>(pt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dotComponent;
	}

	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultComponentRenderable(getVisualizationComponent(
				renderable, ioContainer));
	}
}