package com.rapidminer.ioobjectrenderers;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JComponent;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;
import org.rapidprom.prom.CallProm;

import com.rapidminer.ioobjects.ProcessTreeIOObject.VisualizationType;
import com.rapidminer.ioobjects.ProcessTreeIOObject;
import com.rapidminer.gui.renderer.AbstractRenderer;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.Utilities;

public class ProcessTreeIOObjectRenderer extends AbstractRenderer {

	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		if (renderable instanceof ProcessTreeIOObject) 
		{
			CallProm tp = new CallProm();
			List<Object> parameters = new ArrayList<Object>();
			ProcessTreeIOObject processtree = (ProcessTreeIOObject) renderable;
			ProcessTree pt = processtree.getData();
			parameters.add(pt);
			
			PluginContext pluginContext = processtree.getPluginContext();
			
			JComponent result;
			
			if(((ProcessTreeIOObject) renderable).getVisualizationType().equals(VisualizationType.Processtree)) 
			{
				Object[] runVisualizationPlugin = tp.runPlugin(pluginContext, "XX","Visualize Process tree as Tree" , parameters);
				result = (JComponent) runVisualizationPlugin[0];
			}
			else
			{
				result = tp.runVisualizationPlugin(pluginContext,"x",parameters);
			}
			return result;
		}
		
		return null;
	}
	public Reportable createReportable(Object renderable, IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		
		JComponent panel = (JComponent) getVisualizationComponent(renderable, ioContainer);
		return new DefaultComponentRenderable(Utilities.getSizedPanel(panel,panel, desiredWidth, desiredHeight));
	}

	public String getName() {
		return "ProcessTreerenderer";
	}

}