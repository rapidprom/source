package org.rapidprom.ioobjectrenderers;

public enum PNRepResultIOObjectVisualizationType {

	PROJECT_ON_LOG("Project alignments on log"), PROJECT_ON_MODEL(
			"Project alignments on model");

	private final String name;

	private PNRepResultIOObjectVisualizationType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
