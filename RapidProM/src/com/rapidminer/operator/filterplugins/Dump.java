package com.rapidminer.operator.filterplugins;


import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;
import com.rapidminer.util.WriteToFile1;
import com.rapidminer.util.WriteToFile10;
import com.rapidminer.util.WriteToFile2;
import com.rapidminer.util.WriteToFile3;
import com.rapidminer.util.WriteToFile4;
import com.rapidminer.util.WriteToFile5;
import com.rapidminer.util.WriteToFile6;
import com.rapidminer.util.WriteToFile7;
import com.rapidminer.util.WriteToFile8;
import com.rapidminer.util.WriteToFile9;

public class Dump extends Operator {
	
	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

	public Dump(OperatorDescription description) {
		super(description);
		
		dummyPorts.start();

		getTransformer().addRule(dummyPorts.makePassThroughRule());
}

	public void doWork() throws OperatorException {
		LogService logService = LogService.getGlobal();
		logService.log("start do work Repeat", LogService.NOTE);
		WriteToFile1.getInstance().close();
		WriteToFile2.getInstance().close();
		WriteToFile3.getInstance().close();
		WriteToFile4.getInstance().close();
		WriteToFile5.getInstance().close();
		WriteToFile6.getInstance().close();
		WriteToFile7.getInstance().close();
		WriteToFile8.getInstance().close();
		WriteToFile9.getInstance().close();
		WriteToFile10.getInstance().close();
		dummyPorts.passDataThrough();
		logService.log("end do work Repeat", LogService.NOTE);
	}
}
