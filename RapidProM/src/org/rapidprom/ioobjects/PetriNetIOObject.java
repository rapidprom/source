package org.rapidprom.ioobjects;

import javassist.tools.rmi.ObjectNotFoundException;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

/**
 * @author abolt
 * 
 * Initial markings are referenced directly in the petrinet ioobject.
 */
public class PetriNetIOObject extends AbstractRapidProMIOObject<Petrinet> {

	private static final long serialVersionUID = -4574922526705299348L;
	private Marking marking = null;

	public PetriNetIOObject(Petrinet t, PluginContext context) {
		super(t, context);
	}
	
	public Marking getInitialMarking() throws ObjectNotFoundException{
		if(marking != null)
			return marking;
		else
			throw new ObjectNotFoundException("There is no marking associated with this petri net");
	}
	
	public void setInitialMarking(Marking marking){
		this.marking = marking;
	}

}
