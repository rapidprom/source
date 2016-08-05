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

public class StartXSRunnableOperator extends Operator {

	private final InputPort runnableInput = getInputPorts()
			.createPort("runnable_to_start", XSRunnableIOObject.class);

	private final InputPortExtender dependenciesPort = new InputPortExtender(
			"objects_to_wait_for", getInputPorts(), null, false);

	private final OutputPort runnableOutput = getOutputPorts()
			.createPort("runnable_passed_through");

	public StartXSRunnableOperator(OperatorDescription description) {
		super(description);
		dependenciesPort.start();
		getTransformer().addPassThroughRule(runnableInput, runnableOutput);
	}

	@Override
	public void doWork() throws OperatorException {
		for (InputPort i : dependenciesPort.getManagedPorts()) {
			try {
				((XSRunnable) i.getData(XSRunnableIOObject.class).getArtifact())
						.startXSRunnable();
			} catch (UserError e) {
			}
		}
		XSRunnable runnable = (XSAuthor<?>) runnableInput
				.getData(XSAuthorIOObject.class).getArtifact();
		runnable.startXSRunnable();
		runnableOutput.deliver(runnableInput.getData(XSAuthorIOObject.class));
	}

}
