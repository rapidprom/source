package com.rapidminer.operator.filterplugins;

import com.rapidminer.callprom.ReferenceMainPluginContext;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.util.ProMIOObjectList;

public class ClearProMObjects extends Operator {
	
	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

	public ClearProMObjects(OperatorDescription description) {
		super(description);
		
		dummyPorts.start();

		getTransformer().addRule(dummyPorts.makePassThroughRule());
}

	public void doWork() throws OperatorException {
		// empty the contents of ProMIOObjects
		ProMIOObjectList instance = ProMIOObjectList.getInstance();
		instance.clear();
		ReferenceMainPluginContext instance2 = ReferenceMainPluginContext.getInstance();
		instance2.getCliContextCallProm().setMainPluginContext(null);
		instance2.setCliContextCallProm(null);
		System.gc();
		dummyPorts.passDataThrough();
	}

}
