package com.rapidminer.ioobjectrenderers;

public enum XLogIOObjectVisualizationType {

	EXAMPLE_SET("Example Set"), DEFAULT("Default"), X_DOTTED_CHART(
			"XDottedChart");

	private final String name;

	private XLogIOObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
};
