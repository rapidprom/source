package com.rapidminer.callprom;

import org.rapidprom.external.connectors.prom.RapidProMCLIContext;

public class ReferenceMainPluginContext {
	private static ReferenceMainPluginContext instance = null;
	private RapidProMCLIContext cliContextCallProm = null;
	
	private ReferenceMainPluginContext () {
		
	}
	
	public static ReferenceMainPluginContext getInstance () {
		if (instance == null) {
			instance = new ReferenceMainPluginContext();
		}
		return instance;
	}
	
	public void setCliContextCallProm(RapidProMCLIContext cliContextCallProm) {
		this.cliContextCallProm = cliContextCallProm;
	}
	
	public RapidProMCLIContext getCliContextCallProm() {
		return this.cliContextCallProm;
	}
}
