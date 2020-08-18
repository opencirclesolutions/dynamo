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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.util.ProgressCounter;
import com.ocs.dynamo.utils.DefaultProgressCounter;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * A form that displays of a progress bar while some time consuming process is
 * taking place
 * 
 * @author bas.rutten
 * @param <T> the type of the object that is being processed. This can usually
 *        be "Object" but can e.g. be more specific in case of a file upload you
 *        can provide a byte array.
 */
public abstract class ProgressForm<T> extends BaseCustomComponent implements Progressable {

    private static final long serialVersionUID = -4717815709838453902L;

    /**
     * The screen mode
     * 
     * @author bas.rutten
     *
     */
    public enum ProgressMode {
        PROGRESSBAR, SIMPLE;
    }

    // the polling interval in milliseconds
    public static final int POLL_INTERVAL = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressForm.class);

    // counter for keeping track of the number of processed items
    private ProgressCounter counter = new DefaultProgressCounter();

    // the main layout
    private VerticalLayout mainLayout;

    // the progress bar
    private ProgressBar progressBar;

    // the progress layout
    private VerticalLayout progressLayout;

    // the progress mode - indicates whether to render a progress bar
    private ProgressMode progressMode;

    // the label that displays the status (which percentage is complete?)
    private Text status;

    // the UI from which the process was started
    private UI ui;

    /**
     * Constructor
     * 
     * @param progressMode indicates the mode of the screen (and whether the
     *                     progress bar will be displayed)
     */
    public ProgressForm(UI ui, ProgressMode progressMode) {
        this.ui = ui;
        this.progressMode = progressMode;
    }

    /**
     * Specify what to do after the job completes
     * 
     * @param exceptionOccurred indicates whether an exception occurred
     */
    protected void afterWorkComplete(boolean exceptionOccurred) {
        // overwrite in subclass
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        build();
    }

    @Override
    public void build() {
        formMode();
    }

    /**
     * Constructs a layout with a single button that will start the process when
     * clicked
     * 
     * @param parent  the layout that serves as the parent of the layout to
     *                construct
     * @param caption the caption of the button
     */
    protected void constructSingleButtonLayout(VerticalLayout parent, String caption) {
        HorizontalLayout buttonBar = new DefaultHorizontalLayout(true, true);
        parent.add(buttonBar);

        Button clearButton = new Button(caption);
        clearButton.addClickListener(event -> startWork(null));
        buttonBar.add(clearButton);
    }

    /**
     * Constructs the specific part of the layout
     * 
     * @param main the layout that serves as the container for everything that is
     *             being added
     */
    protected abstract void doBuildLayout(VerticalLayout main);

    /**
     * Method that is called after the job completes
     */
    private void done(boolean exceptionOccurred) {
        afterWorkComplete(exceptionOccurred);
        formMode();
    }

    /**
     * Estimates the current progress based on the counter - override in case of
     * custom progress calculation
     * 
     * @return
     */
    @Override
    public int estimateCurrentProgress() {
        return counter.getCurrent();
    }

    /**
     * Estimates the total size of the process that is carried out
     * 
     * @param t the object being processed (can be null)
     * @return
     */
    protected abstract int estimateSize(T t);

    /**
     * Extracts the OCSRuntimeException from the provided exception
     * 
     * @param t the exception
     * @return
     */
    protected OCSRuntimeException extractRuntimeException(Throwable t) {
        if (t instanceof OCSRuntimeException) {
            return (OCSRuntimeException) t;
        } else if (t.getCause() != null) {
            return extractRuntimeException(t.getCause());
        }
        return null;
    }

    /**
     * Renders the "form" mode which allows the user to input data before starting
     * the
     */
    protected void formMode() {
        if (mainLayout == null) {
            mainLayout = new DefaultVerticalLayout(false, true);

            Text label = new Text(getTitle() == null ? "" : getTitle());
            mainLayout.add(label);

            // add the screen-specific content
            doBuildLayout(mainLayout);
        }

        // disable polling
        ui.setPollInterval(-1);
        removeAll();
        add(mainLayout);
        afterFormModeEntered();
    }

    protected void afterFormModeEntered() {
        // overwrite in subclasses
    }

    public ProgressCounter getCounter() {
        return counter;
    }

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public Text getStatusLabel() {
        return status;
    }

    /**
     * Returns the title to be posted above the form
     * 
     * @return
     */
    protected String getTitle() {
        // overwrite in subclass if needed
        return null;
    }

    /**
     * Generic handling of an exception - tries to extract the OCSRuntimeException
     * if possible
     * 
     * @param ex the exception to handle
     */
    protected void handleException(Exception ex) {
        OCSRuntimeException r = extractRuntimeException(ex);
        if (r != null) {
            showNotification(r.getMessage());
        } else {
            showNotification(ex.getMessage());
        }
    }

    /**
     * Indicates whether the form is valid for processing. By default this method
     * returns <code>true</code>. Override if needed and return false if the process
     * cannot be started
     * 
     * @param t the (optional) object that is being processed
     * @return
     */
    protected boolean isFormValid(T t) {
        return true;
    }

    /**
     * Perform the actual processing
     * 
     * @param t             the optional object that is being processed
     * @param estimatedSize the estimated size of the batch process
     */
    protected abstract void process(T t, int estimatedSize);

    /**
     * Displays a layout that contains a progress bar and a status label
     */
    private void progressMode() {
        // lazily build the layout
        if (progressLayout == null) {
            progressLayout = new DefaultVerticalLayout(true, true);

            progressBar = new ProgressBar();
            progressBar.setHeight("100px");
            progressLayout.add(progressBar);

            status = new Text("");
            progressLayout.add(status);
        }
        progressBar.setValue(0.0f);
        status.setText("");

        removeAll();
        add(progressLayout);
    }

    /**
     * Locks the UI and displays a notification
     * 
     * @param message the message
     * @param type    the type of the notification
     */
    protected void showNotification(String message) {
        if (ui != null) {
            ui.access(() -> showErrorNotification(message));
        }
    }

    /**
     * Marks the process as completed
     * 
     * @param exceptionOccurred whether an exception occurred
     */
    private void signalDone(boolean exceptionOccurred) {
        if (ui != null) {
            ui.access(() -> done(exceptionOccurred));
        } else {
            done(exceptionOccurred);
        }
    }

    /**
     * Start the actual work - this is the method that must be called in order to
     * actually start the processing
     * 
     * @param t the (optional) object that is being processed
     */
    protected final void startWork(final T t) {
        if (isFormValid(t)) {
            if (ProgressMode.SIMPLE.equals(progressMode)) {
                // simply execute the process (without displaying any feedback)
                try {
                    process(t, 0);
                    done(false);
                } catch (RuntimeException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    // exception during size estimation
                    showNotification(ex.getMessage());
                    signalDone(true);
                }
            } else {
                // switch to progress bar mode
                progressMode();

                // start a thread to update the progress
                try {
                    int estimatedSize = estimateSize(t);

                    counter.reset();
                    ui.setPollInterval(POLL_INTERVAL);

                    ProgressBarUpdater updater = new ProgressBarUpdater(ui, this, estimatedSize);

                    // the thread that updates the progress bar
                    Thread updateThread = new Thread(updater);
                    updateThread.start();

                    // the thread that performs the actual work
                    Thread worker = new Thread(() -> {
                        try {
                            process(t, estimatedSize);
                        } finally {
                            updater.setStopped(true);
                            signalDone(false);
                        }
                    });
                    worker.start();
                } catch (RuntimeException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    // exception during size estimation
                    showNotification(ex.getMessage());
                    signalDone(true);
                }
            }
        }
    }

	public UI getUi() {
		return ui;
	}

}
