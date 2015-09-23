package com.rapidminer.operator.conversionplugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;

public class AddSourceAndSinkPlacesTask extends Operator {

	private InputPort inputPN = getInputPorts().createPort("model (ProM Petri Net)", PetriNetIOObject.class);
	private OutputPort outputPN = getOutputPorts().createPort("model (ProM Petri Net)");
	
	public AddSourceAndSinkPlacesTask(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
		getTransformer().addRule( new GenerateNewMDRule(outputPN, PetriNetIOObject.class));
	}
	
	@Override
	public void doWork() throws OperatorException {
		
		PetriNetIOObject petrinet = inputPN.getData(PetriNetIOObject.class);
		Petrinet pn = petrinet.getPn();
		
		Place[] sources = getSources(pn);
		Place[] sinks = getSinks(pn);
		
		Place source = pn.addPlace("Source_");
		Place sink = pn.addPlace("Sink_");
		
		Transition[] fromsource = new Transition[sources.length];
		for(int i = 0 ; i < fromsource.length ; i++)
		{
			fromsource[i] = pn.addTransition("tran_sition"+i);
			fromsource[i].setInvisible(true);
			pn.addArc(source, fromsource[i]);
			pn.addArc(fromsource[i],sources[i]);
		}
		
		Transition[] tosink = new Transition[sinks.length];
		for(int i = 0 ; i < tosink.length ; i++)
		{
			tosink[i] = pn.addTransition("tran_sition"+i);
			tosink[i].setInvisible(true);
			pn.addArc(sinks[i], tosink[i]);
			pn.addArc(tosink[i],sink);
		}
		outputPN.deliver(petrinet);
	}
	
	private Place[] getSinks(Petrinet pn)
	{
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection outEdges = pn.getOutEdges(nextPlace);
			if (outEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		Place[] finalMarking = new Place[places.size()];
		for(int i = 0; i < places.size() ; i++)
			finalMarking[i] = places.get(i);
		
		return finalMarking;
	}
	
	private Place[] getSources(Petrinet pn)
	{
		List<Place> places = new ArrayList<Place>();
		Iterator<Place> placesIt = pn.getPlaces().iterator();
		while (placesIt.hasNext()) {
			Place nextPlace = placesIt.next();
			Collection inEdges = pn.getInEdges(nextPlace);
			if (inEdges.isEmpty()) {
				places.add(nextPlace);
			}
		}
		if(places.isEmpty()) //hardcoded to detect a "start%" transition
		{
			
			Iterator<Transition> iter = pn.getTransitions().iterator();
			while (iter.hasNext()) {
				Transition next = iter.next();
				String name = next.getLabel();
				
//				System.out.println("Transition name before: " + name);
				
				name = name.replace('+',' ').trim().toLowerCase();
				
//				System.out.println("Transition name after: " + name);
				
				if(name.startsWith("start"))
				{
//					System.out.println("start event detected!");
					Collection outEdges = pn.getInEdges(next);
					Iterator<Arc> arcsIt = outEdges.iterator();
					if(arcsIt.hasNext())
					{
						Arc out = arcsIt.next();
						places.add((Place) out.getSource());											
					}
						
				}
			}
		}
			
		Place[] initMarking = new Place[places.size()];
		for(int i = 0; i < places.size() ; i++)
			initMarking[i] = places.get(i);
		
		return initMarking;
	}

}
