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
package com.ocs.dynamo.ui.composite.form.process;

import java.math.BigDecimal;

import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.MathUtils;
import com.vaadin.flow.component.UI;

/**
 * A Runnable that is used to update a progress bar during a long running
 * process
 * 
 * @author bas.rutten
 */
public class ProgressBarUpdater implements Runnable {

	private Progressable progressable;

	private int estimatedSize;

	private volatile float progress;

	private volatile boolean stopped;

	private UI ui;

	/**
	 * Constructor
	 * 
	 * @param progressable  the component that must be updated
	 * @param estimatedSize the estimated size of the process
	 */
	public ProgressBarUpdater(UI ui, Progressable progressable, int estimatedSize) {
		this.ui = ui;
		this.progressable = progressable;
		this.progress = 0.0f;
		this.estimatedSize = estimatedSize;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	@Override
	public void run() {
		while (!stopped && progress < 1.0) {
			try {
				Thread.sleep(ProgressForm.POLL_INTERVAL);
			} catch (InterruptedException e) {
				// do nothing
			}

			ui.access(() -> {
				if (estimatedSize > 0) {
					progress = (float) ((1. * progressable.estimateCurrentProgress()) / (1. * estimatedSize));
				} else {
					progress = 1.0f;
				}
				if (progress > 1.0) {
					progress = 1.0f;
				}
				progressable.getProgressBar().setValue(progress);

				String progressString = VaadinUtils.bigDecimalToString(true, false,
						BigDecimal.valueOf(progress).multiply(MathUtils.HUNDRED));

				MessageService ms = ServiceLocatorFactory.getServiceLocator().getMessageService();
				progressable.getStatusLabel()
						.setText(ms.getMessage("ocs.progress.done", VaadinUtils.getLocale(), progressString));
			});

		}
	}
}
