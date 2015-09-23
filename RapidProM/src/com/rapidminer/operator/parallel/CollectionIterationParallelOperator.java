package com.rapidminer.operator.parallel;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import org.deckfour.xes.model.XLog;

import com.rapidminer.ioobjects.XLogIOObject;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.CollectingPortPairExtender;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.CollectionMetaData;
import com.rapidminer.operator.ports.metadata.MDTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.util.SingleInstanceLock;
import com.rapidminer.util.WriteToFile1;
import com.rapidminer.util.WriteToFile2;
import com.rapidminer.util.WriteToFile3;
import com.rapidminer.util.WriteToFile4;
import com.rapidminer.util.WriteToFile5;

public class CollectionIterationParallelOperator extends OperatorChain {
	
	protected static final String PARAMETER_UNFOLD = "unfold";
	
	private final InputPort collectionInput = getInputPorts().createPort("collection", new CollectionMetaData(new MetaData()));
	private final OutputPort singleInnerSource = getSubprocess(0).getInnerSources().createPort("single");
	private final CollectingPortPairExtender outExtender = new CollectingPortPairExtender("output", getSubprocess(0).getInnerSinks(), getOutputPorts()); 

	public CollectionIterationParallelOperator(OperatorDescription description) {
		super(description, "Iteration");
		outExtender.start();
		getTransformer().addRule(new MDTransformationRule() {
			@Override
			public void transformMD() {
				MetaData md = collectionInput.getMetaData();
				if ((md != null) && (md instanceof CollectionMetaData)) {
					if (getParameterAsBoolean(PARAMETER_UNFOLD)) {
						singleInnerSource.deliverMD(((CollectionMetaData) md).getElementMetaDataRecursive());
					} else {
						singleInnerSource.deliverMD(((CollectionMetaData) md).getElementMetaData());
					}
				} else {
					singleInnerSource.deliverMD(null);
				}
			}
		});
		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
		getTransformer().addRule(outExtender.makePassThroughRule());
		
	}
	
	@Override
	public void doWork() throws OperatorException {
		IOObjectCollection<IOObject> data = collectionInput.getData(IOObjectCollection.class);
		List<IOObject> list;
		if (getParameterAsBoolean(PARAMETER_UNFOLD)) {
			list = data.getObjectsRecursive(); 
		} else {
			list = data.getObjects();
		}
		outExtender.reset();
		//
		ExecutorService pool = Executors.newFixedThreadPool(12);
		Collection<TaskCallable> tasks = new TreeSet<TaskCallable>();
		int counter = 0;
		// maybe the creation of this one causes problems
		SingleInstanceLock instance = SingleInstanceLock.getInstance();
		Lock lock = instance.getLock();
		//Lock lock = new ReentrantLock();
		for (IOObject o : list) {
			if (o instanceof XLogIOObject) {
				XLogIOObject logio = (XLogIOObject) o;
				XLog data2 = logio.getData();
//				WriteToFile3 instance = WriteToFile3.getInstance();
//				instance.write(System.identityHashCode(data2) + "");
//				System.out.println("");
			}
			
			tasks.add(new TaskCallable(this, o, counter, lock));
			counter++;
		}
//		for (TaskCallable task : tasks) {
//			task.call();
//		}
		long p1 = System.currentTimeMillis();
		try {
			pool.invokeAll(tasks);
			System.out.println("I have " + tasks.size() + " tasks!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			pool.shutdown();
			while (true) {
	            try {
	                //System.out.println("Waiting for the service to terminate...");
	                if (pool.awaitTermination(5, TimeUnit.SECONDS)) {
	                    break;
	                }
	            } catch (InterruptedException e) {
	            }
	        }
	        //System.out.println("Done cleaning");
		}
		pool = null;
		lock = null;
		 WriteToFile1.getInstance().write((System.currentTimeMillis() - p1)+"");
		//System.gc();
		// close the writing
//		WriteToFile1.getInstance().close();
//		WriteToFile2.getInstance().close();
//		WriteToFile3.getInstance().close();
		// make this parallel
//		for (IOObject o : list) {
//			if (setIterationMacro) {
//				String iterationString = Integer.toString(currentIteration + macroIterationOffset);
//				getProcess().getMacroHandler().addMacro(iterationMacroName, iterationString);
//			}
//			singleInnerSource.deliver(o);
//			getSubprocess(0).execute();
//			outExtender.collect();
//			currentIteration++;
//		}
		// end making parallel
	}
	
	/*
	 * Specific class for running the replay of a subproblem in its seperate
	 * thread.
	 */
	class TaskCallable implements Callable<Boolean>, Comparable<TaskCallable> {
		private CollectionIterationParallelOperator parent = null;
		private final int id;
		private final IOObject obj;
		private final Lock lock;

		public TaskCallable(CollectionIterationParallelOperator parent, IOObject obj, int id, Lock lock) {
			this.parent = parent;
			this.obj = obj;
			this.id = id;
			this.lock = lock;
		}

		public Boolean call() throws OperatorException {
//			synchronized(parent) {
//				singleInnerSource.deliver(obj);
//			}
			long p2 = System.currentTimeMillis();
			lock.lock();
			singleInnerSource.deliver(obj);
			executeOnClone(null, getSubprocess(0), null, lock, getLogger());
			outExtender.collect();
			lock.unlock();
			// getSubprocess(0).execute();
//			synchronized(parent) {
//				outExtender.collect();
//			}
			WriteToFile2.getInstance().write((System.currentTimeMillis() - p2)+"");
			return true;
		}

		/*
		 * 
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(TaskCallable o) {
			return Integer.compare(id, o.id);
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {		
		List<ParameterType> types = super.getParameterTypes();		
		types.add(new ParameterTypeBoolean(PARAMETER_UNFOLD, "Determines if the input collection is unfolded.", false));
		// ADD PARAMETER FOR THE NUMBER OF THREADS
		return types;
	}
	
    public static void executeOnClone(Runnable preExcecution, ExecutionUnit unit, Runnable postExecutiuon, Lock lock, Logger logger) throws OperatorException {
        ExecutionUnit clone;
        if (preExcecution != null) {
            preExcecution.run();
        }
        long p4 = System.currentTimeMillis(); 
        clone = cloneWithInput(unit);
        WriteToFile4.getInstance().write((System.currentTimeMillis() - p4)+"");
        lock.unlock();
        long p3 = System.currentTimeMillis();
        //logger.fine("Cloned execution unit "+unit.getName()+". Start executing");
        clone.execute();
        //logger.fine("Completed execution of clone "+clone.getName()+". Obtaining lock.");
        WriteToFile3.getInstance().write((System.currentTimeMillis() - p3)+"");
        lock.lock();
        long p5 = System.currentTimeMillis();
        // copy result of clone to original execution unit
        for (int i = 0; i < clone.getInnerSinks().getNumberOfPorts(); i++) {
            InputPort clonedPort = clone.getInnerSinks().getPortByIndex(i);
            IOObject data = clonedPort.getAnyDataOrNull();
            if (data != null) {
                unit.getInnerSinks().getPortByIndex(i).receive(data.copy());
            }
        }
        WriteToFile5.getInstance().write((System.currentTimeMillis() - p5)+"");
        // how about destroying the clone
//        int code1 = Integer.MIN_VALUE;
//        int code2 = Integer.MIN_VALUE;
//        OutputPort oport = clone.getInnerSources().getPortByIndex(0); // this is the log
//        code1 = System.identityHashCode(oport.getDataOrNull(XLogIOObject.class).getPromLog());
//        InputPort iport = clone.getInnerSinks().getPortByIndex(0); // this is the apn
//        code2 = System.identityHashCode(iport.getDataOrNull(AcceptingPetriNetIOObject.class).getAp());
//        System.out.println();
//        while (iterator.hasNext()) {
//        	OutputPort next = iterator.next();
//        	XLogIOObject data = next.getDataOrNull(XLogIOObject.class);
//        	if (data != null) {
//        		code1 = System.identityHashCode(data.getPromLog());
//        	}
//        	System.out.println();
//        }
//        while (iterator2.hasNext()) {
//        	InputPort next = iterator2.next();
//        	AcceptingPetriNetIOObject data = next.getDataOrNull(AcceptingPetriNetIOObject.class);
//        	if (data != null) {
//        		code2 = System.identityHashCode(data.getAp());
//        	}
//        	System.out.println();
//        }
        if (postExecutiuon != null) {
            postExecutiuon.run();
        }
        clone.freeMemory();
        clone = null;
//        WriteToFile1 instance = WriteToFile1.getInstance();
//		instance.write("C:log:" + code1 + "apn:" + code2 + "\n");
    }
    
    private static ExecutionUnit cloneWithInput(ExecutionUnit unit) {
        ExecutionUnit clone = new ExecutionUnit(unit.getEnclosingOperator(), unit.getName());
        for (Port orig : unit.getInnerSources().getAllPorts()) {
            clone.getInnerSources().createPort(orig.getName());
        }
        for (Port orig : unit.getInnerSinks().getAllPorts()) {
            clone.getInnerSinks().createPort(orig.getName());
        }

        clone.cloneExecutionUnitFrom(unit, true);
        //clone.cloneExecutionUnitFrom(unit, false);
        for (int i = 0; i < unit.getInnerSources().getNumberOfPorts(); i++) {
            OutputPort orig = unit.getInnerSources().getPortByIndex(i);
            IOObject data = orig.getAnyDataOrNull();
            if (data != null) {
                clone.getInnerSources().getPortByIndex(i).deliver(data.copy());
            }
        }
        return clone;
    }

}
