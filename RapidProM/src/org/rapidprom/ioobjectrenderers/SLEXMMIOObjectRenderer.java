package org.rapidprom.ioobjectrenderers;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.database.metamodel.dapoql.QueryResult;
import org.processmining.database.metamodel.dapoql.ui.components.MetaModelInspectorPanel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logprojection.LogProjectionPlugin;
import org.processmining.logprojection.LogView;
import org.processmining.logprojection.plugins.dottedchart.ui.DottedChartInspector;
import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.processmining.plugins.dottedchartanalysis.DottedChartAnalysis;
import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;
import org.processmining.plugins.log.ui.logdialog.LogDialogInitializer;
import org.processmining.plugins.log.ui.logdialog.SlickerOpenLogSettings;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjectrenderers.abstr.AbstractMultipleVisualizersRenderer;
import org.rapidprom.ioobjects.SLEXMMIOObject;
import org.rapidprom.ioobjects.SLEXMMSubSetIOObject;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.util.XLogUtils;
import org.rapidprom.util.XLogUtils.AttributeTypes;
import org.rapidprom.util.XLogUtils.TableModelXLog;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.gui.renderer.DefaultComponentRenderable;
import com.rapidminer.gui.renderer.data.ExampleSetDataRenderer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.io.AbstractDataReader.AttributeColumn;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.report.Reportable;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.container.Range;

public class SLEXMMIOObjectRenderer extends
		AbstractMultipleVisualizersRenderer<SLEXMMIOObjectVisualizationType> {

	public SLEXMMIOObjectRenderer() {
		super(EnumSet.allOf(SLEXMMIOObjectVisualizationType.class),
				"SLEXMM SubSet renderer");
	}

//	private Component exampleSetComponent = null;
//	private WeakReference<XLog> exampleSetLog = null;
	private Component defaultComponent = null;
	private WeakReference<SLEXMMStorageMetaModelImpl> defaultMM = null;
//	private Component dottedChartComponent = null;
//	private WeakReference<XLog> dottedLog = null;
//	private Component dottedChartLegacyComponent = null;
//	private WeakReference<XLog> dottedLegacyLog = null;
//	private Attribute[] attributes = null;

	protected Component visualizeRendererOption(
			SLEXMMIOObjectVisualizationType e, Object renderable,
			IOContainer ioContainer) {

		System.out.println("looking for renderer!");
		Component result;
		switch (e) {
//		case EXAMPLE_SET:
//			result = createExampleSetComponet(renderable, ioContainer);
//			break;
		default:
		case DEFAULT:
			result = createDefaultVisualizerComponent(renderable, ioContainer);
			break;
		}
		return result;
	}

//	protected Component createExampleSetComponet(Object renderable,
//			IOContainer ioContainer) {
//		XLogIOObject object = (XLogIOObject) renderable;
//		XLog log = object.getArtifact();
//		if (exampleSetComponent == null || exampleSetLog == null
//				|| !(log.equals(exampleSetLog.get()))) {
//			MemoryExampleTable table = null;
//			ExampleSet es = null;
//
//			try {
//				TableModelXLog convertLogToStringTable = XLogUtils
//						.convertLogToStringTable(object.getArtifact(), true);
//
//				table = createStructureTable(convertLogToStringTable);
//				es = fillTable(table, convertLogToStringTable);
//
//			} catch (Exception error) {
//				error.printStackTrace();
//			}
//			ExampleSetDataRenderer renderer = new ExampleSetDataRenderer();
//			exampleSetComponent = renderer.getVisualizationComponent(es,
//					ioContainer);
//			exampleSetLog = new WeakReference<XLog>(log);
//		}
//		return exampleSetComponent;
//	}
//
//	private MemoryExampleTable createStructureTable(
//			TableModelXLog convertedLog) {
//		ExampleSetMetaData metaData = new ExampleSetMetaData();
//		List<Attribute> attributes = new LinkedList<Attribute>();
//		for (int i = 0; i < convertedLog.getColumnCount(); i++) {
//			String columnName = convertedLog.getColumnName(i);
//			AttributeTypes columnType = convertedLog.getColumnType(i);
//			AttributeMetaData amd = null;
//			if (columnType.equals(AttributeTypes.CONTINUOUS)) {
//				attributes.add(AttributeFactory.createAttribute(columnName,
//						Ontology.NUMERICAL));
//				amd = new AttributeMetaData(columnName, Ontology.NUMERICAL);
//				amd.setRole(AttributeColumn.REGULAR);
//				amd.setNumberOfMissingValues(new MDInteger(0));
//				List<Double> minAndMaxValueColumn = getMinAndMaxValueColumn(
//						convertedLog, columnName);
//				amd.setValueRange(
//						new Range(minAndMaxValueColumn.get(0),
//								minAndMaxValueColumn.get(1)),
//						SetRelation.EQUAL);
//			} else if (columnType.equals(AttributeTypes.DISCRETE)) {
//				attributes.add(AttributeFactory.createAttribute(columnName,
//						Ontology.NOMINAL));
//				amd = new AttributeMetaData(columnName, Ontology.NOMINAL);
//				amd.setRole(AttributeColumn.REGULAR);
//				amd.setNumberOfMissingValues(new MDInteger(0));
//			} else if (columnType.equals(AttributeTypes.DATE)) {
//				attributes.add(AttributeFactory.createAttribute(columnName,
//						Ontology.DATE_TIME));
//				amd = new AttributeMetaData(columnName, Ontology.DATE_TIME);
//				amd.setRole(AttributeColumn.REGULAR);
//				amd.setNumberOfMissingValues(new MDInteger(0));
//				List<Double> minAndMaxValueColumn = getMinAndMaxValueColumn(
//						convertedLog, columnName);
//				amd.setValueRange(
//						new Range(minAndMaxValueColumn.get(0),
//								minAndMaxValueColumn.get(1)),
//						SetRelation.EQUAL);
//			} else if (columnType.equals(AttributeTypes.STRING)) {
//				attributes.add(AttributeFactory.createAttribute(columnName,
//						Ontology.NOMINAL));
//				amd = new AttributeMetaData(columnName, Ontology.NOMINAL);
//				amd.setRole(AttributeColumn.REGULAR);
//				amd.setNumberOfMissingValues(new MDInteger(0));
//			} else if (columnType.equals(AttributeTypes.BOOLEAN)) {
//				attributes.add(AttributeFactory.createAttribute(columnName,
//						Ontology.BINOMINAL));
//				amd = new AttributeMetaData(columnName, Ontology.NOMINAL);
//				amd.setRole(AttributeColumn.REGULAR);
//				amd.setNumberOfMissingValues(new MDInteger(0));
//			}
//			metaData.addAttribute(amd);
//		}
//		// convert the list to array
//		Attribute[] attribArray = new Attribute[attributes.size()];
//		for (int i = 0; i < attributes.size(); i++) {
//			attribArray[i] = attributes.get(i);
//		}
//		metaData.setNumberOfExamples(convertedLog.getRowCount());
//
//		this.attributes = attribArray;
//		MemoryExampleTable memoryExampleTable = new MemoryExampleTable(
//				attributes);
//		return memoryExampleTable;
//	}

//	private ExampleSet fillTable(MemoryExampleTable table,
//			TableModelXLog convertedLog) {
//		DataRowFactory factory = new DataRowFactory(
//				DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
//		// now add per row
//		for (int i = 0; i < convertedLog.getRowCount(); i++) {
//			// fill strings
//			String[] strings = new String[convertedLog.getColumnCount()];
//			for (int j = 0; j < convertedLog.getColumnCount(); j++) {
//				strings[j] = convertedLog.getValueAt(i, j).toString();
//			}
//			DataRow dataRow = factory.create(strings, attributes);
//			table.addDataRow(dataRow);
//		}
//		ExampleSet createExampleSet = table.createExampleSet();
//		return createExampleSet;
//	}

//	private List<Double> getMinAndMaxValueColumn(TableModelXLog convertedLog,
//			String nameCol) {
//		double min = Double.MAX_VALUE;
//		double max = Double.MIN_VALUE;
//		int intCol = convertedLog.getNameForColumn(nameCol);
//		for (int i = 0; i < convertedLog.getRowCount(); i++) {
//			Object valueAt = convertedLog.getValueAt(i, intCol);
//			if (valueAt instanceof String) {
//				try {
//					double parseDouble = Double.parseDouble((String) valueAt);
//					min = parseDouble < min ? parseDouble : min;
//					max = parseDouble > max ? parseDouble : max;
//				} catch (Exception e) {
//					// do nothing with it.
//				}
//			}
//		}
//		List<Double> doubleList = new ArrayList<Double>();
//		doubleList.add(min);
//		doubleList.add(max);
//		return doubleList;
//	}

//	@SuppressWarnings("unused")
//	protected Component createDefaultVisualizerComponent(Object renderable,
//			IOContainer ioContainer) {
//		XLogIOObject logioobject = (XLogIOObject) renderable;
//		XLog xLog = logioobject.getArtifact();
//		if (defaultComponent == null || defaultLog == null
//				|| !(xLog.equals(defaultLog.get()))) {
//			try {
//
//				PluginContext pluginContext = ProMPluginContextManager
//						.instance().getContext();
//
//				LogDialogInitializer i = new LogDialogInitializer();
//				SlickerOpenLogSettings o = new SlickerOpenLogSettings();
//
//				ClassLoader c = Thread.currentThread().getContextClassLoader();
//
//				defaultComponent = o.showLogVis(pluginContext, xLog);
//				defaultLog = new WeakReference<XLog>(xLog);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return defaultComponent;
//	}
	
	@SuppressWarnings("unused")
	protected Component createDefaultVisualizerComponent(Object renderable,
			IOContainer ioContainer) {
		SLEXMMIOObject oobject = (SLEXMMIOObject) renderable;
		SLEXMMStorageMetaModelImpl mm = oobject.getArtifact();
		
		if (defaultComponent == null || defaultMM == null
				|| !(mm.equals(defaultMM.get()))) {
			try {

//				PluginContext pluginContext = ProMPluginContextManager
//						.instance().getContext();

//				LogDialogInitializer i = new LogDialogInitializer();
//				SlickerOpenLogSettings o = new SlickerOpenLogSettings();

//				ClassLoader c = Thread.currentThread().getContextClassLoader();

//				defaultComponent = o.showLogVis(pluginContext, xLog);
				defaultComponent = new MetaModelInspectorPanel(mm,false);
				defaultMM = new WeakReference<SLEXMMStorageMetaModelImpl>(mm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultComponent;
	}

//	protected Component createDottedChartVisualizerComponent(Object renderable,
//			IOContainer ioContainer) {
//		XLogIOObject logioobject = (XLogIOObject) renderable;
//		XLog xLog = logioobject.getArtifact();
//		if (dottedChartComponent == null || dottedLog == null
//				|| !(xLog.equals(dottedLog.get()))) {
//			try {
//
//				PluginContext pluginContext = ProMPluginContextManager
//						.instance().getContext();
//
//				LogView result = new LogView(xLog);
//				DottedChartInspector panel = LogProjectionPlugin
//						.visualize(pluginContext, result);
//				dottedChartComponent = (JComponent) panel;
//				dottedLog = new WeakReference<XLog>(xLog);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return dottedChartComponent;
//
//	}

	public Reportable createReportable(Object renderable,
			IOContainer ioContainer, int desiredWidth, int desiredHeight) {
		return new DefaultComponentRenderable(
				getVisualizationComponent(renderable, ioContainer));
	}
}