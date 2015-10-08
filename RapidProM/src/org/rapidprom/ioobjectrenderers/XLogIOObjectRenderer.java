package org.rapidprom.ioobjectrenderers;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
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
	// private Component xDottedChartComponent = null; //TO-DO
	// private WeakReference<XLog> xDottedLog = null;

	protected Component visualizeRendererOption(XLogIOObjectVisualizationType e,
			Object renderable, IOContainer ioContainer) {
		Component result;
		switch (e) {
		case EXAMPLE_SET:
			result = createExampleSetComponet(renderable, ioContainer);
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
		XLog log = object.getXLog();
		if (exampleSetComponent == null || exampleSetLog == null
				|| !(log.equals(exampleSetLog.get()))) {
			final List<String> columnNames = new ArrayList<String>();
			System.out.println("Render!");
			ColumnNamesLog columnNames2 = XLogUtils
					.getColumnNames(object.getPromLog());
			columnNames.addAll(columnNames2.getAttribsTrace());
			columnNames.addAll(columnNames2.getAttribsEvents());
			try {
				TableModelXLog convertLogToStringTable = XLogUtils
						.convertLogToStringTable(object.getPromLog(), false);
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
		XLog xLog = logioobject.getXLog();
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

	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultComponentRenderable(
				getVisualizationComponent(renderable, ioContainer));
	}

}