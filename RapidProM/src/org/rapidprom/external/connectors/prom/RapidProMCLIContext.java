package org.rapidprom.external.connectors.prom;

import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;

import com.rapidminer.callprom.ReferenceMainPluginContext;

/**
 * 
 * @author rmans
 *
 */
public class RapidProMCLIContext extends CLIContext {

	protected CLIPluginContext mainPluginContext;

	public RapidProMCLIContext() {
		super();
		mainPluginContext = new CLIPluginContext(this, "Main Plugin Context");
		ReferenceMainPluginContext instance = ReferenceMainPluginContext
				.getInstance();
		instance.setCliContextCallProm(this);
	}

	@Override
	public CLIPluginContext getMainPluginContext() {
		return mainPluginContext;
	}

	public void setMainPluginContext(CLIPluginContext pc) {
		this.mainPluginContext = pc;
	}

}
