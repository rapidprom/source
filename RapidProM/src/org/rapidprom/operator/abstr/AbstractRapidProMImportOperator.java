package org.rapidprom.operator.abstr;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.UserError;

import java.io.File;

public abstract class AbstractRapidProMImportOperator extends Operator {

    protected static final String PARAMETER_LABEL_FILENAME = "Filename";
    protected static final String PARAMETER_LABEL_IMPORTERS = "Importer";

    public AbstractRapidProMImportOperator(OperatorDescription description) {
        super(description);
    }

    protected boolean checkFileParameterMetaData(String key)
            throws UserError {
        boolean result;
        File file = getParameterAsFile(key);
        if (!file.exists()) {
            throw new UserError(this, "301", file);
        } else if (!file.canRead()) {
            throw new UserError(this, "302", file, "");
        } else {
            result = true;
        }
        return result;
    }

}
