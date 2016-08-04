package org.rapidprom.ioobjectrenderers;

public enum SLEXMMIOSubSetObjectVisualizationType {
 
//	EXAMPLE_SET("Example Set"),
//	DOTTED_CHART("Dotted Chart"),
//	DOTTED_CHART_L("Dotted Chart (Legacy)"),
	DEFAULT("Default");
	
	private final String name;

	private SLEXMMIOSubSetObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
};
