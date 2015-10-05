package org.rapidprom.operators.abstr;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;

public class AbstractRapidProMOperator extends Operator {

    protected static final String PARAMETER_LABEL_FILENAME = "Filename";
    protected static final String PARAMETER_LABEL_IMPORTERS = "Importer";

    public AbstractRapidProMOperator(OperatorDescription description) {
        super(description);
    }

}
