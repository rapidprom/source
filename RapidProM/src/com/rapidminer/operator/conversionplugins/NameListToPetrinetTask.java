package com.rapidminer.operator.conversionplugins;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.ioobjects.PetriNetIOObject;
import com.rapidminer.ioobjects.ProMContextIOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;

public class NameListToPetrinetTask extends Operator{

	private InputPort inputContext = getInputPorts().createPort("context (ProM Context)", ProMContextIOObject.class);
	private InputPort input = getInputPorts().createPort("example set (Data Table)", new ExampleSetMetaData());
	
	private OutputPort output = getOutputPorts().createPort("model (ProM Petri Net)");
	private OutputPort outExa = getOutputPorts().createPort("example set (Data Table)");
	
	public NameListToPetrinetTask(OperatorDescription description) {
		super(description);
		getTransformer().addRule( new GenerateNewMDRule(output, PetriNetIOObject.class));
	}
	
	@Override
	public void doWork() throws OperatorException {
	
		ProMContextIOObject context = inputContext.getData(ProMContextIOObject.class);
		PluginContext pluginContext = context.getPluginContext();
		
		ExampleSet data = input.getData(ExampleSet.class);
		
		Iterator<Example> iterator = data.iterator();
	
		SortedSet<String> sorted = new TreeSet<String>(new NameComparator());
		
		
		Example ex = null;
		while (iterator.hasNext()) {
			ex = iterator.next();
			sorted.add(ex.getValueAsString(data.getAttributes().get("Activity")));
		}
		
		
		
		//create Petrinet
		Petrinet a = PetrinetFactory.newPetrinet("Name");
		
		//create starting point
		Place last = a.addPlace("Start");
		Place first = null;
		Transition in = null;
		Transition activity = null;
		Transition back = null;
		
		//for each activity, add a "block"
		String[] list = sorted.toArray(new String[sorted.size()]);
		for(int i = 0 ; i < list.length ; i++)
		{
			//gets into the block
			in = a.addTransition("t_in"+i);
			in.setInvisible(true);
			a.addArc(last, in);
			
			//now we create the block
			first = a.addPlace("pf"+i);
			a.addArc(in, first);
			
			last = a.addPlace("pl"+i);
			
			//the transition corresponding to the activity
			activity = a.addTransition(list[i]);
			a.addArc(first, activity);
			a.addArc(activity, last);
			
			//the loop back
			back = a.addTransition("t_b"+i);
			back.setInvisible(true);
			a.addArc(last, back);
			a.addArc(back, first);
		}
		
		//ending of the petrinet
		first = a.addPlace("End");
		in = a.addTransition("last");
		in.setInvisible(true);
		a.addArc(last, in);
		a.addArc(in,first);
		
		PetriNetIOObject outputnet = new PetriNetIOObject(a);
		outputnet.setPluginContext(pluginContext);
		output.deliver(outputnet);
		
		Object[][] output = new Object[list.length][1];
		
		for(int i = 0 ; i < list.length ; i++)
			output[i][0] = list[i];
		
		ExampleSet es =	ExampleSetFactory.createExampleSet(output);
		outExa.deliver(es);
	}
	
	
	public static class NameComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			// TODO Auto-generated method stub
			o1 = o1.toLowerCase();
			o2 = o2.toLowerCase();
			
			if(o1.matches(o2))
				return 0;
			else if(o1.endsWith("exam"))
				return 1;
			else if(o2.endsWith("exam"))
				return -1;
			else if(o1.length() < o2.length())
				return -1;
			else if(o1.length() > o2.length())
				return 1;
			else
				return o1.compareTo(o2);
		}

	}
	
	

}
