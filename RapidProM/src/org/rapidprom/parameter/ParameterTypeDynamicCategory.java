package org.rapidprom.parameter;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.MetaDataChangeListener;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.MetaDataProvider;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.container.Pair;

/**
 * The ParameterTypeDynamicCategory, allows us to change the contents of a
 * {@link ParameterTypeCategory}, depending on some inputPort's meta data.
 * Additionally this class introduces the option to query, given the selected
 * index, what the corresponding object is.
 * 
 * @author svzelst
 *
 * @param <T>
 *            indicates what underlying java object the user wants to choose.
 */
public abstract class ParameterTypeDynamicCategory<T>
		extends ParameterTypeCategory {

	private static final long serialVersionUID = 5610913750316933718L;

	private final T[] defaultValues;
	private final String[] defaultValuesToString;

	private final MetaDataProvider metaDataProvider;

	private T[] values = null;
	private String[] valuesToString = new String[0];

	public ParameterTypeDynamicCategory(String key, String description,
			String[] defaultValuesToString, T[] defaultValues,
			int defaultValueIndex, boolean expert, final InputPort inputPort) {
		this(key, description, defaultValuesToString, defaultValues,
				defaultValueIndex, expert, new MetaDataProvider() {

					@Override
					public void addMetaDataChangeListener(
							MetaDataChangeListener l) {
						inputPort.registerMetaDataChangeListener(l);

					}

					@Override
					public MetaData getMetaData() {
						if (inputPort != null) {
							return inputPort.getMetaData();
						} else {
							return null;
						}
					}

					@Override
					public void removeMetaDataChangeListener(
							MetaDataChangeListener l) {
						inputPort.removeMetaDataChangeListener(l);

					}
				});
	}

	public ParameterTypeDynamicCategory(String key, String description,
			String[] defaultValuesToString, T[] defaultValues,
			int defaultValueIndex, boolean expert,
			MetaDataProvider metaDataProvider) {
		super(key, description, defaultValuesToString, defaultValueIndex,
				expert);
		this.defaultValues = defaultValues;
		this.defaultValuesToString = defaultValuesToString;
		this.valuesToString = defaultValuesToString;
		this.values = defaultValues;
		this.metaDataProvider = metaDataProvider;
	}

	public int getIndexOf(String string) {
		for (int i = 0; i < valuesToString.length; i++) {
			if (valuesToString[i].equals(string)) {
				return Integer.valueOf(i);
			}
		}
		// try to interpret string as number
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public T valueOf(int index) throws IndexOutOfBoundsException {
		if (index > values.length) {
			throw new IndexOutOfBoundsException("The index is not defined");
		}
		return values[index];
	}

	public String[] getValuesToString() {
		return valuesToString;
	}

	public void setValuesToString(String[] valuesToString) {
		this.valuesToString = valuesToString;
	}

	public T[] getDefaultValues() {
		return defaultValues;
	}

	public String[] getDefaultValuesToString() {
		return defaultValuesToString;
	}

	public void setValues(T[] values) {
		this.values = values;
	}

	public MetaDataProvider getMetaDataProvider() {
		return metaDataProvider;
	}

	@Override
	public String[] getValues() {
		Pair<String[], T[]> newValues = updateValues();
		valuesToString = newValues.getFirst();
		values = newValues.getSecond();
		return valuesToString;
	}

	@Override
	public String toString(Object value) {
		try {
			if (value == null)
				return null;
			int index = Integer.parseInt(value.toString());
			if (index >= valuesToString.length)
				return "";
			return super.toString(valuesToString[index]);
		} catch (NumberFormatException e) {
			return super.toString(value);
		}
	}

	@Override
	public int getIndex(String string) {
		for (int i = 0; i < valuesToString.length; i++) {
			if (valuesToString[i].equals(string)) {
				return Integer.valueOf(i);
			}
		}
		// try to interpret string as number
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			// take the first of the *probably* new list
			return 0;
		}
	}

	protected abstract Pair<String[], T[]> updateValues();

}
