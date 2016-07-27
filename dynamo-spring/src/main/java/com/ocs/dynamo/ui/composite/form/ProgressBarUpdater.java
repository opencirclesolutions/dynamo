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
package com.ocs.dynamo.ui.composite.form;

import java.math.BigDecimal;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.ui.UI;

/**
 * A Runnable that is used to update a progress bar during a long running process
 * 
 * @author bas.rutten
 */
public class ProgressBarUpdater implements Runnable {

    // the component that hosts the long-running process
    private Progressable progressable;

    // the estimated size of the long-running process
    private int estimatedSize;

    // the current progress (fraction between 0 and 1)
    private volatile float progress;

    // flag variable to indicate that the process is stopped
    private volatile boolean stopped;

    /**
     * Constructor
     * 
     * @param progressable
     *            the component that must be updated
     * @param estimatedSize
     *            the estimated size of the process
     */
    public ProgressBarUpdater(Progressable progressable, int estimatedSize) {
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

            UI.getCurrent().access(new Runnable() {

                @Override
                public void run() {
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
                            BigDecimal.valueOf(progress * 100));
                    progressable.getStatusLabel().setValue(progressString + " done");
                }
            });
        }
    }
}
