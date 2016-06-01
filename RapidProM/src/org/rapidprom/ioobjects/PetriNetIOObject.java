package org.rapidprom.ioobjects;

import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;
import org.rapidprom.ioobjects.abstr.AbstractRapidProMIOObject;

import javassist.tools.rmi.ObjectNotFoundException;

/**
 * @author abolt
 * 
 *         Initial markings are referenced directly in the petrinet ioobject.
 */
public class PetriNetIOObject extends AbstractRapidProMIOObject<Petrinet> {

	private static final long serialVersionUID = -4574922526705299348L;
	private Marking initialMarking = null;
	private Marking finalMarking = null;

	public PetriNetIOObject(Petrinet t, Marking i, Marking f,
			PluginContext context) {
		super(t, context);
		setInitialMarking(i);
		setFinalMarking(f);
	}

	public Marking getInitialMarking() throws ObjectNotFoundException {
		if (initialMarking != null)
			return initialMarking;
		else
			throw new ObjectNotFoundException(
					"There is no initial marking associated with this petri net");
	}
	
	public boolean hasInitialMarking() {
		if (initialMarking != null)
			return true;
		else
			return false;
	}

	public void setInitialMarking(Marking marking) {
		initialMarking = marking;
		if (initialMarking != null)
			this.context.addConnection(new InitialMarkingConnection(
					this.getArtifact(), initialMarking));
	}

	public Marking getFinalMarking() throws ObjectNotFoundException {
		if (finalMarking != null)
			return finalMarking;
		else
			throw new ObjectNotFoundException(
					"There is no final marking associated with this petri net");
	}

	public boolean hasFinalMarking() {
		if (finalMarking != null)
			return true;
		else
			return false;
	}

	public Marking[] getFinalMarkingAsArray() throws ObjectNotFoundException {

		List<Marking> fM = new ArrayList<Marking>();
		if (finalMarking.size() > 0) {
			for (Place place : finalMarking) {
				Marking m = new Marking();
				m.add(place);
				fM.add(m);
			}
			return fM.toArray(new Marking[finalMarking.size()]);
		}
		else
			return new Marking[]{new Marking()};

	}

	public void setFinalMarking(Marking marking) {
		finalMarking = marking;
		if (finalMarking != null)
			this.context.addConnection(new FinalMarkingConnection(
					this.getArtifact(), finalMarking));
	}

}
