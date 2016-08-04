package org.rapidprom.ioobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.rapidprom.ioobjectrenderers.SLEXMMIOSubSetObjectVisualizationType;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class SLEXMMSubSetIOObject extends AbstractRapidProMIOObject<SLEXMMStorageMetaModelImpl> {

	private static final long serialVersionUID = -1323690731245887615L;
	
	private SLEXMMIOSubSetObjectVisualizationType visType;
	private Set<Object> results;
	private HashMap<Object,HashSet<Integer>> mapResults;
	private Class<?> type;

	public SLEXMMSubSetIOObject(SLEXMMStorageMetaModelImpl mm, HashMap<Object,HashSet<Integer>> mapResults, Set<Object> results, Class<?> type) {
		super(mm,null);
		this.results = results;
		this.mapResults = mapResults;
		this.type = type;
	}

	public void setVisualizationType(SLEXMMIOSubSetObjectVisualizationType visType) {
		this.visType = visType;
		
	}
	public SLEXMMIOSubSetObjectVisualizationType getVisualizationType(){
		return visType;
	}
	
	public Set<Object> getResults() {
		return results;
	}

	public HashMap<Object,HashSet<Integer>> getMapResults() {
		return mapResults;
	}
	
	public Class<?> getType() {
		return type;
	}
	
}
