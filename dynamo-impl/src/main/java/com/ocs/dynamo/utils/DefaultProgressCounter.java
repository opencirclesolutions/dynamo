/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
