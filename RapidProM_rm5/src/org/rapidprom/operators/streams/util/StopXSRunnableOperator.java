package org.rapidprom.operators.streams.util;

import org.processmining.stream.core.interfaces.XSAuthor;
import org.processmining.stream.core.interfaces.XSRunnable;
import org.rapidprom.ioobjects.streams.XSAuthorIOObject;
import org.rapidprom.ioobjects.streams.XSRunnableIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;

public class StopXSRunnableOperator extends Operator {

	private final InputPort runnableInput = getInputPorts()
			.createPort("runnable_to_stop", XSRunnableIOObject.class);

	private final InputPortExtender artifactsPort = new InputPortExtender(
			"objects_to_stop", getInputPorts(), null, false);

	private final OutputPort runnableOutput = getOutputPorts()
			.createPort("runnable_passed_through");

	public StopXSRunnableOperator(OperatorDescription description) {
		super(description);
		artifactsPort.start();
		getTransformer().addPassThroughRule(runnableInput, runnableOutput);
	}

	@Override
	public void doWork() throws OperatorException {
		for (InputPort i : artifactsPort.getManagedPorts()) {
			try {
				((XSRunnable) i.getData(XSRunnableIOObject.class).getArtifact())
						.stopXSRunnable();
			} catch (UserError e) {
			}
		}
		XSRunnable runnable = (XSAuthor<?>) runnableInput
				.getData(XSAuthorIOObject.class).getArtifact();
		runnable.stopXSRunnable();
		runnableOutput.deliver(runnableInput.getData(XSAuthorIOObject.class));
	}

}
