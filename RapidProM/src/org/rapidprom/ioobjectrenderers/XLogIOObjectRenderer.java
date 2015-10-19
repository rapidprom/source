package org.rapidprom.ioobjectrenderers;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logprojection.LogProjectionPlugin;
import org.processmining.logprojection.LogView;
import org.processmining.logprojection.plugins.dottedchart.ui.DottedChartPanel;
import org.processmining.plugins.dottedchartanalysis.DottedChartAnalysis;
import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;
import org.processmining.plugins.log.ui.logdialog.LogDialogInitializer;
import org.processmining.plugins.log.ui.logdialog.SlickerOpenLogSettings;
import org.rapidprom.AbstractMultipleVisualizersRenderer;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.report.Reportable;
import com.rapidminer.util.XLogUtils;
import com.rapidminer.util.XLogUtils.ColumnNamesLog;
import com.rapidminer.util.XLogUtils.TableModelXLog;

public class XLogIOObjectRenderer extends
		AbstractMultipleVisualizersRenderer<XLogIOObjectVisualizationType> {

	public XLogIOObjectRenderer() {
		super(EnumSet.allOf(XLogIOObjectVisualizationType.class),
				"XLog renderer");
	}

	private Component exampleSetComponent = null;
	private WeakReference<XLog> exampleSetLog = null;
	private Component defaultComponent = null;
	private WeakReference<XLog> defaultLog = null;
	private Component dottedChartComponent = null;
	private WeakReference<XLog> dottedLog = null;
	private Component dottedChartLegacyComponent = null;
	private WeakReference<XLog> dottedLegacyLog = null;

	protected Component visualizeRendererOption(XLogIOObjectVisualizationType e,
			Object renderable, IOContainer ioContainer) {
		
		System.out.println("looking for renderer!");
		Component result;
		switch (e) {		
		case DOTTED_CHART:
			result = createDottedChartVisualizerComponent(renderable, ioContainer);
			break;
		case EXAMPLE_SET:
			result = createExampleSetComponet(renderable, ioContainer);
			break;
		case DOTTED_CHART_L:
			result = createDottedChartLegacyVisualizerComponent(renderable, ioContainer);
			break;
		default:
		case DEFAULT:
			result = createDefaultVisualizerComponent(renderable, ioContainer);
			break;
		}
		return result;
	}

	protected Component createExampleSetComponet(Object renderable,
			IOContainer ioContainer) {
		XLogIOObject object = (XLogIOObject) renderable;
		XLog log = object.getArtifact();
		if (exampleSetComponent == null || exampleSetLog == null
				|| !(log.equals(exampleSetLog.get()))) {
			final List<String> columnNames = new ArrayList<String>();
			System.out.println("Render!");
			ColumnNamesLog columnNames2 = XLogUtils
					.getColumnNames(object.getArtifact());
			columnNames.addAll(columnNames2.getAttribsTrace());
			columnNames.addAll(columnNames2.getAttribsEvents());
			try {
				TableModelXLog convertLogToStringTable = XLogUtils
						.convertLogToStringTable(object.getArtifact(), false);
				return new ExtendedJScrollPane(new ExtendedJTable(
						convertLogToStringTable, true, true));
			} catch (Exception error) {
				error.printStackTrace();
			}
			exampleSetComponent = new ExtendedJScrollPane(
					new ExtendedJTable(new DefaultTableModel(), true, true));
			exampleSetLog = new WeakReference<XLog>(log);
		}
		return exampleSetComponent;
	}

	@SuppressWarnings("unused")
	protected Component createDefaultVisualizerComponent(Object renderable,
			IOContainer ioContainer) {
		XLogIOObject logioobject = (XLogIOObject) renderable;
		XLog xLog = logioobject.getArtifact();
		if (defaultComponent == null || defaultLog == null
				|| !(xLog.equals(defaultLog.get()))) {
			try {

				PluginContext pluginContext = ProMPluginContextManager
						.instance().getContext();

				LogDialogInitializer i = new LogDialogInitializer();
				SlickerOpenLogSettings o = new SlickerOpenLogSettings();

				ClassLoader c = Thread.currentThread().getContextClassLoader();

				defaultComponent = o.showLogVis(pluginContext, xLog);
				defaultLog = new WeakReference<XLog>(xLog);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultComponent;
	}
	
	protected Component createDottedChartVisualizerComponent(Object renderable,
			IOContainer ioContainer) {
		XLogIOObject logioobject = (XLogIOObject) renderable;
		XLog xLog = logioobject.getArtifact();
		if (dottedChartComponent == null || dottedLog == null
				|| !(xLog.equals(dottedLog.get()))) {
			try {

				PluginContext pluginContext = ProMPluginContextManager
						.instance().getContext();

				
				LogView result = new LogView(xLog);
				DottedChartPanel panel = LogProjectionPlugin.visualize(pluginContext, result);
				dottedChartComponent = (JComponent) panel;
				dottedLog = new WeakReference<XLog>(xLog);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dottedChartComponent;
	
	}
	
	protected Component createDottedChartLegacyVisualizerComponent(Object renderable,
			IOContainer ioContainer) {
		XLogIOObject logioobject = (XLogIOObject) renderable;
		XLog xLog = logioobject.getArtifact();
		if (dottedChartLegacyComponent == null || dottedLegacyLog == null
				|| !(xLog.equals(dottedLegacyLog.get()))) {
			try {

				PluginContext pluginContext = ProMPluginContextManager
						.instance().getContext();

				DottedChartModel result = new DottedChartModel(pluginContext, xLog);
				dottedChartLegacyComponent = new DottedChartAnalysis(pluginContext, result);
				dottedLegacyLog = new WeakReference<XLog>(xLog);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dottedChartLegacyComponent;
	
	}

	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultComponentRenderable(
				getVisualizationComponent(renderable, ioContainer));
	}

}