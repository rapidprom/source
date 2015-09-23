package com.rapidminer.configuration;

import java.io.File;

public class GlobalProMParameters {
	
	private static GlobalProMParameters globalParameters = new GlobalProMParameters();
	
	private File promFolder = null;
	
	private String promPackagesFolder = null;
	
	private String proMLocation = "";
	
	public GlobalProMParameters () {
		
	}
	
	public static GlobalProMParameters getInstance() {
		return globalParameters;
	}

	public File getPromFolder() {
		return promFolder;
	}

	public void setPromFolder(File promFolder) {
		this.promFolder = promFolder;
	}

	public String getProMLocation() {
		return proMLocation;
	}

	public void setProMLocation(String proMLocation) {
		this.proMLocation = proMLocation;
	}

	public String getPromPackagesFolder() {
		return promPackagesFolder;
	}

	public void setPromPackagesFolder(String promPackagesFolder) {
		this.promPackagesFolder = promPackagesFolder;
	}
	
	
	

}
