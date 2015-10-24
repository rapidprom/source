package org.rapidprom.operators.io;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.petrinets.FileFormat;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToEPNML;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMExporter;

import com.rapidminer.operator.OperatorDescription;

public class ExportPetriNetOperator extends
		AbstractRapidProMExporter<PetriNetIOObject, Petrinet, FileFormat> {

	public ExportPetriNetOperator(OperatorDescription description) {
		super(description, PetriNetIOObject.class,
				EnumSet.allOf(FileFormat.class).toArray(
						new FileFormat[EnumSet.allOf(FileFormat.class).size()]),
				FileFormat.PNML);
	}

	@Override
	protected void writeToFile(File file, Petrinet object, FileFormat format)
			throws IOException {
		switch (format) {
		case EPNML:
			PnmlExportNetToEPNML exporterEPNML = new PnmlExportNetToEPNML();
			exporterEPNML
					.exportPetriNetToEPNMLFile(
							ProMPluginContextManager.instance()
									.getFutureResultAwareContext(
											PnmlExportNetToEPNML.class),
							object, file);
			break;
		case PNML:
		default:
			PnmlExportNetToPNML exporterPNML = new PnmlExportNetToPNML();
			exporterPNML
					.exportPetriNetToPNMLFile(
							ProMPluginContextManager.instance()
									.getFutureResultAwareContext(
											PnmlExportNetToPNML.class),
							object, file);
			break;
		}
	}

}
