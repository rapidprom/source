package org.rapidprom.operators.ports.metadata;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.AbstractPrecondition;
import com.rapidminer.operator.ports.metadata.CompatibilityLevel;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.InputMissingMetaDataError;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.MetaDataUnderspecifiedError;

public class ExampleSetNumberOfAttributesPrecondition
		extends AbstractPrecondition {

	private final int numberOfColumns;

	public ExampleSetNumberOfAttributesPrecondition(InputPort inputPort,
			final int numColumns) {
		super(inputPort);
		this.numberOfColumns = numColumns;
	}

	@Override
	public void check(MetaData metaData) {
		final InputPort inputPort = getInputPort();
		if (metaData == null) {
			inputPort.addError(new InputMissingMetaDataError(inputPort,
					ExampleSet.class, null));
		} else {
			if (metaData instanceof ExampleSetMetaData) {
				ExampleSetMetaData emd = (ExampleSetMetaData) metaData;
				if (emd.getAllAttributes().size() < numberOfColumns) {
					// TODO: customize the error message
					inputPort.addError(
							new MetaDataUnderspecifiedError(inputPort));
				}
			}
		}

	}

	@Override
	public String getDescription() {
		return "<em>expects:</em> ExampleSet";
	}

	@Override
	public boolean isCompatible(MetaData input, CompatibilityLevel level) {
		return ExampleSet.class.isAssignableFrom(input.getObjectClass());
	}

	@Override
	public void assumeSatisfied() {
		getInputPort().receiveMD(new ExampleSetMetaData());
	}

	@Override
	public MetaData getExpectedMetaData() {
		return new ExampleSetMetaData();
	}

}
