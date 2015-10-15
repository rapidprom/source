package org.rapidprom.ioobjectrenderers;

public enum XLogIOObjectVisualizationType {

	DEFAULT("Default"), 
	EXAMPLE_SET("Example Set"),
	DOTTED_CHART("DottedChart"),
	DOTTED_CHART_L("DottedChart (Legacy)");
	
	private final String name;

	private XLogIOObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
};
