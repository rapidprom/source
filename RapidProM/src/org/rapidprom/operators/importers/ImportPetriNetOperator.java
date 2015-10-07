package org.rapidprom.operators.importers;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.parameter.*;

import org.processmining.framework.plugin.PluginContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.String;

import com.rapidminer.ioobjects.MarkingIOObject;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.petrinet.mining.alphaminer.AlphaMiner;
import org.processmining.plugins.pnml.Pnml;
import org.processmining.plugins.pnml.importing.PnmlImportNet;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;
import org.rapidprom.external.connectors.prom.ProMPluginContextManager;
import org.rapidprom.ioobjects.PetriNetIOObject;
import org.rapidprom.operators.abstr.AbstractRapidProMImportOperator;

public class ImportPetriNetOperator extends AbstractRapidProMImportOperator {

	//private Parameter importerParameter = null;
	
	private OutputPort outputPetriNet = getOutputPorts().createPort("model (ProM Petri Net)");

	public ImportPetriNetOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(outputPetriNet, PetriNetIOObject.class));
	}

    private static String[] SUPPORTED_PETRI_NET_FORMATS = new String[]{"pnml"};

    @Override
    public void doWork() throws OperatorException {
        Logger logger = LogService.getRoot();
        logger.log(Level.INFO, "Start: importing petri net");
        long time = System.currentTimeMillis();

        Petrinet pn;
        if (checkFileParameterMetaData(PARAMETER_LABEL_FILENAME)) {
            pn = importPetrinet(getParameterAsFile(PARAMETER_LABEL_FILENAME));
            PetriNetIOObject petriNetIOObject = new PetriNetIOObject(pn);
            petriNetIOObject.setPluginContext(
                    ProMPluginContextManager.instance().getContext());
            outputPetriNet.deliver(petriNetIOObject);
            
            logger.log(Level.INFO, "End: importing petri net (" + (System.currentTimeMillis() - time)/1000 + " sec)");
        }
    }

    private Petrinet importPetrinet(File file) {
    	Petrinet result = null;    	
        try {
        	PnmlImportUtils utils = new PnmlImportUtils();
        	PluginContext context = ProMPluginContextManager.instance().getContext();
        	
        	Pnml pnml = utils.importPnmlFromStream(context, new FileInputStream(file), file.getName(),file.length());
        	if (pnml == null) return null;
        	
        	PetrinetGraph net = PetrinetFactory.newPetrinet(pnml.getLabel());
        	
        	result = (Petrinet) utils.connectNet(context, pnml, net);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return result;
    }

    
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> parameterTypes = super.getParameterTypes();

        ParameterTypeFile pnmlFileParameter = new ParameterTypeFile(
                PARAMETER_LABEL_FILENAME, "File to open", false, SUPPORTED_PETRI_NET_FORMATS);       
        parameterTypes.add(pnmlFileParameter);
        
        return parameterTypes;
    }
}