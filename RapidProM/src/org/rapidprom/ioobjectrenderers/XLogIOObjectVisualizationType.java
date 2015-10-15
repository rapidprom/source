package org.rapidprom.ioobjectrenderers;

public enum XLogIOObjectVisualizationType {

	DEFAULT("Default"), 
	EXAMPLE_SET("Example Set"),
	DOTTED_CHART("Dotted Chart"),
	DOTTED_CHART_L("Dotted Chart (Legacy)");
	
	private final String name;

	private XLogIOObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
};
