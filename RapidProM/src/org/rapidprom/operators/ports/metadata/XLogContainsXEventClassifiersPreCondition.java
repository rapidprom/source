package org.rapidprom.operators.ports.metadata;

import org.rapidprom.ioobjects.XLogIOObject;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.InputMissingMetaDataError;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.MetaDataUnderspecifiedError;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;

public class XLogContainsXEventClassifiersPreCondition extends
		SimplePrecondition {

	protected InputPort inputPort;

	public XLogContainsXEventClassifiersPreCondition(InputPort inputPort) {
		super(inputPort, null, false);
		this.inputPort = inputPort;
	}

	public void makeAdditionalChecks(MetaData received) {
		if (received == null || !(received instanceof XLogIOObjectMetaData)) {
			inputPort.addError(new MetaDataUnderspecifiedError(inputPort));
		} else {
			XLogIOObjectMetaData recCast = (XLogIOObjectMetaData) received;
			if (recCast.getXEventClassifiers().isEmpty()) {
				inputPort.addError(new InputMissingMetaDataError(inputPort,
						XLogIOObject.class));
			}
		}
	}

}
