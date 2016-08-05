package org.rapidprom.operators.io;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.processmining.eventstream.authors.staticeventstream.StaticEventStreamFileFormat;
import org.processmining.eventstream.authors.staticeventstream.plugins.XSStaticXSEventStreamExportPlugin;
import org.processmining.eventstream.core.interfaces.XSStaticXSEventStream;
import org.rapidprom.ioobjects.streams.event.XSStaticXSEventStreamIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMExporterOperator;

import com.rapidminer.operator.OperatorDescription;

public class ExportXSStaticXSEventStream extends
		AbstractRapidProMExporterOperator<XSStaticXSEventStreamIOObject, XSStaticXSEventStream, StaticEventStreamFileFormat> {

	public ExportXSStaticXSEventStream(OperatorDescription description) {
		super(description, XSStaticXSEventStreamIOObject.class, EnumSet
				.allOf(StaticEventStreamFileFormat.class)
				.toArray(new StaticEventStreamFileFormat[EnumSet
						.allOf(StaticEventStreamFileFormat.class).size()]),
				StaticEventStreamFileFormat.EVST);
	}

	@Override
	protected void writeToFile(File file, XSStaticXSEventStream object,
			StaticEventStreamFileFormat format) throws IOException {
		switch (format) {
		case EVST:
			XSStaticXSEventStreamExportPlugin.export(object, file);
			break;
		}
	}

}
