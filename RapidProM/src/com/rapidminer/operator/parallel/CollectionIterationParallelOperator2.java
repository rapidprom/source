package com.rapidminer.operator.parallel;

import java.io.File;
import java.util.List;

import com.rapidminer.AsynchronousTask;
import com.rapidminer.ConcurrencyTools;
import com.rapidminer.ConcurrentOperationHelper;
import com.rapidminer.callprom.ClassLoaderUtils;
import com.rapidminer.configuration.GlobalProMParameters;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.collections.CollectionIterationOperator;
import com.rapidminer.tools.config.ConfigurationManager;

public class CollectionIterationParallelOperator2 extends CollectionIterationOperator {
	
//	private ConcurrentOperationHelper<?> concurrentOperationHelper = new ConcurrentOperationHelper<Object>(this) {
//		protected void run(Object argument) throws OperatorException {
//			CollectionIterationParallelOperator2.super.doWork();
//		}
//	};
	
	private ConcurrentOperationHelper<?> concurrentOperationHelper = new ConcurrentOperationHelper<Object>(this) {
		protected void run(Object argument) throws OperatorException {
			System.out.println("");
		}
	};

	public CollectionIterationParallelOperator2(OperatorDescription description) {
		super(description);
		//loadRequiredClasses();
		ConcurrencyTools.installThreadPoolParameters(this);
	}
	
	@Override
	public void doWork() throws OperatorException {
//		concurrentOperationHelper.run(null, false);
		concurrentOperationHelper.executeAsynchronously(new AsynchronousTask() {
			@Override
			public void run() throws OperatorException {
				CollectionIterationParallelOperator2.super.doWork();
			}
		});
	}
	
	
//	protected void executeSubprocess() throws OperatorException {
//		ConcurrencyTools.executeOnClone(null, getSubprocess(0), null, concurrentOperationHelper.getLock(), getLogger());
//		// getSubprocess(0).execute();
//	}	

	
//	@Override
//	public List<ParameterType> getParameterTypes() {
//		loadRequiredClasses();
//		return super.getParameterTypes();
//	}

}
