package org.rapidprom.operators.importers;

import com.rapidminer.ioobjects.CPNModelIOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;
import org.processmining.plugins.cpnet.ColouredPetriNet;
import org.processmining.plugins.cpnet.LoadCPNModelFromFile;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.operators.abstr.AbstractRapidProMOperator;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportCPNModelOperator extends AbstractRapidProMOperator {

    private OutputPort output = getOutputPorts().createPort("Model (Colored Petri Net)");

    public ImportCPNModelOperator(OperatorDescription description) {
        super(description);
        getTransformer().addRule(new GenerateNewMDRule(output, CPNModelIOObject.class));
    }

    @Override
    public void doWork() throws OperatorException {
        Logger logger = LogService.getRoot();
        logger.log(Level.INFO, "Start reading CPN model");

        if (checkFileParameterMetaData(PARAMETER_LABEL_FILENAME)) {
            File file = getParameterAsFile(PARAMETER_LABEL_FILENAME);

            ColouredPetriNet net = null;
            try {
                net = LoadCPNModelFromFile.importColouredPetriNetFromStream(ProMPluginContextManager.instance().getFutureResultAwareContext(LoadCPNModelFromFile.class),
                        new FileInputStream(file), file.getName(), file.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
            CPNModelIOObject cpnIOObject = new CPNModelIOObject(net);
            output.deliver(cpnIOObject);
            logger.log(Level.INFO, "End reading CPN model");
        } else {
            logger.log(Level.WARNING, "End reading CPN model -> File could not be read");
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> parameterTypes = super.getParameterTypes();
        ParameterTypeFile parameterTypeFile = new ParameterTypeFile(
                PARAMETER_LABEL_FILENAME, "CPN model", "cpn", false, false);
        parameterTypes.add(parameterTypeFile);
        return parameterTypes;
    }
}