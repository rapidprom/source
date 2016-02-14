package org.rapidprom.operators.io;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.deckfour.xes.model.XLog;
import org.processmining.log.LogFileFormat;
import org.processmining.plugins.log.exporting.ExportLogMxml;
import org.processmining.plugins.log.exporting.ExportLogMxmlGz;
import org.processmining.plugins.log.exporting.ExportLogXes;
import org.processmining.plugins.log.exporting.ExportLogXesGz;
import org.rapidprom.ioobjects.XLogIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMExporterOperator;

import com.rapidminer.operator.OperatorDescription;

public class ExportXLogOperator extends
		AbstractRapidProMExporterOperator<XLogIOObject, XLog, LogFileFormat> {

	public ExportXLogOperator(OperatorDescription description) {
		super(description, XLogIOObject.class,
				EnumSet.allOf(LogFileFormat.class)
						.toArray(new LogFileFormat[EnumSet
								.allOf(LogFileFormat.class).size()]),
				LogFileFormat.XES);
	}

	protected void writeToFile(File file, XLog log, LogFileFormat format)
			throws IOException {
		switch (format) {
		case MXML:
			ExportLogMxml.export(log, file);
			break;
		case MXML_GZ:
			ExportLogMxmlGz.export(log, file);
			break;
		case XES_GZ:
			ExportLogXesGz.export(log, file);
			break;
		case XES:
		default:
			ExportLogXes.export(log, file);
			break;
		}
	}
}
