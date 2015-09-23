package com.rapidminer.util;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;

 
public abstract class DataRowFactory2 {
 
//	public abstract DataRow createRow(Iterable<String> inputRow);
// 
//	public static DataRowFactory2 withFullStopDecimalSeparator(Iterable<Attribute> attributes) {
//		//checkNotNull(attributes);
//		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
//		return new DataRowFactoryImpl(attributes, factory);
//	}
// 
//	public static DataRowFactory2 withCommaDecimalSeparator(Iterable<Attribute> attributes) {
//		//checkNotNull(attributes);
//		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, ',');
//		return new DataRowFactoryImpl(attributes, factory);
//	}
// 
//	private DataRowFactory2() {}
// 
//	private static final class DataRowFactoryImpl extends DataRowFactory2 {
// 
//		private final DataRowFactory factory;
//		private final Attribute[] attributes;
// 
//		DataRowFactoryImpl(Iterable<Attribute> attributes, DataRowFactory factory){
//			assert factory != null;
//			assert attributes != null;
//			this.attributes = toArray(attributes, Attribute.class);
//			this.factory = factory;
//		}
//		public DataRow createRow(Iterable<String> inputRow) {
//			//checkNotNull(inputRow);
//			String[] strings = toArray(inputRow, String.class);
//			return factory.create(strings, attributes);
//		}
//	}
}