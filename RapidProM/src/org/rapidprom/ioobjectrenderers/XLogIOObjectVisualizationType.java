package org.rapidprom.ioobjectrenderers;

public enum XLogIOObjectVisualizationType {

	DEFAULT("Default"), 
	EXAMPLE_SET("Example Set"),
	X_DOTTED_CHART("XDottedChart");

	private final String name;

	private XLogIOObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
};
