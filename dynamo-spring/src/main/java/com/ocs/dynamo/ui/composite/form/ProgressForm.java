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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

/**
 * A form that displays of a progress bar while some time consuming process is taking place
 * 
 * @author bas.rutten
 * @param <T>
 *            the type of the object that is being processed. This can usually be "Object" but can
 *            e.g. be more specific in case of a file upload
 */
public abstract class ProgressForm<T> extends BaseCustomComponent implements Progressable {

    // The mode in which the screen operates
    public enum ProgressMode {
        PROGRESSBAR, SIMPLE;
    }

    public static final int POLL_INTERVAL = 500;

    private static final long serialVersionUID = -4717815709838453902L;

    private static final Logger LOGGER = Logger.getLogger(ProgressForm.class);

    // counter for keeping track of the number of processed items
    private volatile AtomicInteger counter = new AtomicInteger();

    // the main layout
    private Layout mainLayout;

    // the progress bar
    private ProgressBar progressBar;

    // the progress layout
    private Layout progressLayout;

    // the progress mode - indicates whether to render a progress bar
    private ProgressMode progressMode;

    // the label that displays the status (which percentage is complete?)
    private Label status;

    /**
     * Constructor
     * 
     * @param progressMode
     *            indicates the mode of the screen (and whether the progress bar will be displayed)
     */
    public ProgressForm(ProgressMode progressMode) {
        this.progressMode = progressMode;
    }

    /**
     * Specify what to do after the work is complete
     */
    protected void afterWorkComplete() {
        formMode();
    }

    @Override
    public void attach() {
        super.attach();
        build();
    }

    @Override
    public void build() {
        formMode();
    }

    /**
     * Constructs the specific part of the layout
     * 
     * @param main
     *            the layout that serves as the container for everything that is being added
     */
    protected abstract void doBuildLayout(Layout main);

    /**
     * Estimates the current progress - overwrite this method if you want to use a custom mechanism
     * for updating the progress
     * 
     * @return
     */
    @Override
    public int estimateCurrentProgress() {
        return counter.get();
    }

    /**
     * Estimates the size of the process that is carried out
     * 
     * @param uploaded
     * @return
     */
    protected abstract int estimateSize(T t);

    /**
     * Renders the "form" mode which allows the user to input data before starting the
     */
    protected void formMode() {
        if (mainLayout == null) {
            mainLayout = new DefaultVerticalLayout(false, true);

            Label label = new Label(getTitle());
            mainLayout.addComponent(label);

            // add the screen-specific content
            doBuildLayout(mainLayout);
        }

        // disable polling
        UI.getCurrent().setPollInterval(-1);
        setCompositionRoot(mainLayout);
    }

    public AtomicInteger getCounter() {
        return counter;
    }

    @Override
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public Label getStatusLabel() {
        return status;
    }

    /**
     * Returns the title to be posted above the form
     * 
     * @return
     */
    protected abstract String getTitle();

    /**
     * Indicates whether the form is valid for processing. By default this method returns
     * <code>true</code>. Override if needed.
     * 
     * @param t
     *            the (optional) object that is being processed
     * @return
     */
    protected boolean isFormValid(T t) {
        return true;
    }

    /**
     * Prepares the screen for the actual processing - this is the method that must be called from
     * an action listener in order to start the process
     * 
     * @param t
     *            the (optional) object that is being processed
     * @param fileName
     */
    protected void prepare(final T t) {
        if (isFormValid(t)) {
            if (ProgressMode.SIMPLE.equals(progressMode)) {
                // simply execute the process (without displaying any feedback)
                process(t, 0);
                afterWorkComplete();
            } else {
                // switch to progress bar mode
                progressMode();

                // start a thread to update the progress
                try {
                    final int estimatedSize = estimateSize(t);

                    counter.set(0);
                    UI.getCurrent().setPollInterval(POLL_INTERVAL);

                    final ProgressBarUpdater updater = new ProgressBarUpdater(this, estimatedSize);

                    // the thread that repaints the progress bar
                    Thread thread = new Thread(updater);
                    thread.start();

                    // the thread that performs the actual work
                    Thread worker = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                process(t, estimatedSize);
                            } finally {
                                updater.setStopped(true);
                                getUI().getSession().lock();
                                try {
                                    afterWorkComplete();
                                } finally {
                                    getUI().getSession().unlock();
                                }
                            }
                        }
                    });
                    worker.start();
                } catch (OCSRuntimeException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    // exception during size estimation
                    showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    getUI().getSession().lock();
                    try {
                        afterWorkComplete();
                    } finally {
                        getUI().getSession().unlock();
                    }
                }
            }
        }
    }

    /**
     * Perform the actual processing
     * 
     * @param t
     *            the optional object that is being processed
     * @param estimatedSize
     *            the estimated size of the batch process
     */
    protected abstract void process(T t, int estimatedSize);

    /**
     * Displays a layout that contains a progress bar and a status label
     */
    private void progressMode() {
        // lazily build the layout
        if (progressLayout == null) {
            progressLayout = new DefaultVerticalLayout(true, true);

            progressBar = new ProgressBar(0.0f);
            progressBar.setSizeFull();
            progressLayout.addComponent(progressBar);

            status = new Label();
            progressLayout.addComponent(status);
        }
        progressBar.setValue(0.0f);
        status.setValue("");

        setCompositionRoot(progressLayout);
    }

    public void setCounter(AtomicInteger counter) {
        this.counter = counter;
    }

    /**
     * Locks the UI and displays a notification
     * 
     * @param message
     * @param type
     */
    protected void showNotification(String message, Notification.Type type) {
        getUI().getSession().lock();
        try {
            Notification.show(message, type);
        } finally {
            getUI().getSession().unlock();
        }
    }

    protected OCSRuntimeException extractRuntimeException(Throwable t) {
        if (t instanceof OCSRuntimeException) {
            return (OCSRuntimeException) t;
        } else if (t.getCause() != null) {
            return extractRuntimeException(t.getCause());
        }
        return null;
    }

}
