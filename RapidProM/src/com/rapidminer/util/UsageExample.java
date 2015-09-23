package com.rapidminer.util;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.tools.Ontology;
 
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
public class UsageExample {
 
//	public static void main(String[] args) {
//		List<Attribute> attributes = createAttributes();
//		Map<Attribute, String> roles = createRoles(attributes);
// 
//		ExampleTable table = createExampleTable(attributes, inputRows());
//		ExampleSet es = table.createExampleSet(roles);
//		System.out.println(es);
//	}
// 
//	static ExampleTable createExampleTable(List<Attribute> attributes, Iterable<Row> inputRows) {
//		MemoryExampleTable table = new MemoryExampleTable(attributes);
//		DataRowFactory2 factory = DataRowFactory2.withFullStopDecimalSeparator(attributes);
//		for(Row row : inputRows){
//			DataRow dataRow = factory.createRow(row);
//			table.addDataRow(dataRow);
//		}
//		return table;
//	}
// 
///* ***********************
//Static setup methods
//* ***********************/
// 
//	static List<Attribute> createAttributes() {
//		return ImmutableList.of(
//				AttributeFactory.createAttribute("teamID", Ontology.NOMINAL),
//				AttributeFactory.createAttribute("size", Ontology.INTEGER),
//				AttributeFactory.createAttribute("leader", Ontology.NOMINAL),
//				AttributeFactory.createAttribute("number of qualified employees", Ontology.INTEGER),
//				AttributeFactory.createAttribute("leader changed", Ontology.BINOMINAL),
//				AttributeFactory.createAttribute("average years of experience", Ontology.INTEGER),
//				AttributeFactory.createAttribute("structure", Ontology.BINOMINAL));
//	}
// 
//	static List<String> inputData() {
//		return ImmutableList.of(
//				"team_0, 5, Mr. Miller, 4, no, 9, flat",
//				"team_1, 19, Mrs. Green, 3, yes, 8, flat",
//				"team_2, 16, Mrs. Hansc, 2, no, 3, flat",
//				"team_3, 9, Mr. Chang, 6, yes, 3, flat",
//				"team_4, 17, Mr. Chang, 5, yes, 1, hierarchical");
//	}
// 
//	static List<Row> inputRows(){
//		ImmutableList.Builder<Row> data = ImmutableList.builder();
//		for(String line: inputData()){
//			data.add(newRow(line));
//		}
//		return data.build();
//	}
// 
//	static Row newRow(final String line){
//		return new Row() {
//			public Iterator<String> iterator() {
//				return Splitter.on(',').split(line).iterator();
//			}
//		};
//	}
// 
//	static Map<Attribute, String> createRoles(List<Attribute> attributes){
//		return ImmutableMap.of(attributes.get(0), Attributes.ID_NAME);
//	}
}
