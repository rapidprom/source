package org.rapidprom.operators.experimental;

import java.util.HashMap;

public class DataModelMMKey {
	private String name;
	private String source_class;
	private String target_class;
	private HashMap<String,String> attMap;
	private int relationshipId = -1;
	
	public DataModelMMKey(String name, String source_class, String target_class, int relationshipId) {
		this.name = name;
		this.source_class = source_class;
		this.target_class = target_class;
		this.attMap = new HashMap<>();
		this.relationshipId = relationshipId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSourceClass() {
		return source_class;
	}
	
	public String getTargetClass() {
		return target_class;
	}
	
	public HashMap<String,String> getAttributesMap() {
		return attMap;
	}
	
	public int getRelationshipId() {
		return relationshipId;
	}
}
