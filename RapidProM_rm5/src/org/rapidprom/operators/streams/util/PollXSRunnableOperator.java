package org.rapidprom.operators.streams.util;

import org.processmining.stream.core.interfaces.XSRunnable;
import org.rapidprom.ioobjects.streams.XSRunnableIOObject;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;

public class PollXSRunnableOperator extends Operator {

	private static final long SLEEP_TIME = 125;

	private final InputPort runnableInput = getInputPorts()
			.createPort("runnable_to_poll", XSRunnableIOObject.class);

	private final InputPortExtender dependenciesPort = new InputPortExtender(
			"objects_to_wait_for", getInputPorts(), null, false);

	private final OutputPort runnableOutput = getOutputPorts()
			.createPort("generator");

	public PollXSRunnableOperator(OperatorDescription description) {
		super(description);
		dependenciesPort.start();
		getTransformer().addPassThroughRule(runnableInput, runnableOutput);
	}

	@Override
	public void doWork() throws OperatorException {
		XSRunnable runnable = (XSRunnable) runnableInput
				.getData(XSRunnableIOObject.class).getArtifact();
		while (runnable.isRunning()) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		runnableOutput.deliver(runnableInput.getData(XSRunnableIOObject.class));
	}

}
