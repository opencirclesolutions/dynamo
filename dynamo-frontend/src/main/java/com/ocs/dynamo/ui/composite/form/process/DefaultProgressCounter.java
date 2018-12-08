package com.ocs.dynamo.ui.composite.form.process;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultProgressCounter implements ProgressCounter {

	private volatile AtomicInteger counter = new AtomicInteger();

	@Override
	public void increment() {
		counter.incrementAndGet();
	}

	@Override
	public int getCurrent() {
		return counter.get();
	}

	@Override
	public void incrementBy(int value) {
		counter.addAndGet(value);
	}

	@Override
	public void reset() {
		counter.set(0);
	}

}
