package org.rapidprom.ioobjectrenderers;

public enum ProcessTreeIOObjectVisualizationType {

	DEFAULT("Default"), 
	DOT("Dot");

	private final String name;

	private ProcessTreeIOObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
