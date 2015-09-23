/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.promcontext;

import java.io.File;

import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.rapidprom.prom.CallProm;

import com.rapidminer.configuration.GlobalProMParameters;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.tools.LogService;
import com.rapidminer.util.ProMIOObjectList;

/**
 * 
 * @author Ronny Mans
 */
public class ProMContextTask extends Operator {

	/** defining the ports */
	private OutputPort output = getOutputPorts().createPort(
			"context (ProM Context)");

	/**
	 * The default constructor needed in exactly this signature
	 */
	public ProMContextTask(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new GenerateNewMDRule(output, ProMContextIOObject.class));
	}

	@Override
	public void doWork() throws OperatorException {
		// instantiate ProMContext
		LogService logService = LogService.getGlobal();
		logService.log("start do work prom context", LogService.NOTE);
		System.out.println("SYSOUT");
		// get parameters
		File promLocation = null;
		// ProMConfigurable promConfigurable = (ProMConfigurable)
		// instance.lookup("ProMConfig", "promconfig", null);
		// String promLocationStr =
		// promConfigurable.getParameter(ProMConfigurator.LOCATION_PROM_PACKAGES);
		GlobalProMParameters instance = GlobalProMParameters.getInstance();
		String promLocationStr = instance.getProMLocation();
		logService.log("promLocationStr:" + promLocationStr, LogService.NOTE);
		// the location of prom.ini
		promLocation = new File(promLocationStr);
		CallProm tp = new CallProm();
		CLIPluginContext promContext = tp.instantiateProMContext(promLocation);
		PluginContext childContext = promContext.createChildContext("test");
		ProMContextIOObject pcioobject = new ProMContextIOObject(childContext);
		ProMIOObjectList instanceIOObjectList = ProMIOObjectList.getInstance();
		instanceIOObjectList.addToList(pcioobject);
		output.deliver(pcioobject);
		logService.log("end do work prom context", LogService.NOTE);
	}

}
