package org.rapidprom.ioobjects;

import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.rapidprom.ioobjectrenderers.SLEXMMIOObjectVisualizationType;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

public class SLEXMMIOObject extends AbstractRapidProMIOObject<SLEXMMStorageMetaModelImpl> {

	private static final long serialVersionUID = -1323690731245887615L;
	
	private SLEXMMIOObjectVisualizationType visType;

	public SLEXMMIOObject(SLEXMMStorageMetaModelImpl mm) {
		super(mm,null);
	}

	public void setVisualizationType(SLEXMMIOObjectVisualizationType visType) {
		this.visType = visType;
		
	}
	public SLEXMMIOObjectVisualizationType getVisualizationType(){
		return visType;
	}

	
}
