package com.ocs.dynamo.utils;

import java.util.concurrent.atomic.AtomicInteger;

import com.ocs.dynamo.util.ProgressCounter;

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
