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

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A form that displays of a progress bar while some time consuming process is taking place
 * 
 * @author bas.rutten
 * @param <T>
 *            the type of the object that is being processed. This can usually be "Object" but can
 *            e.g. be more specific in case of a file upload you can provide a byte array.
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

	private UI ui = UI.getCurrent();

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
		// overwrite in subclass
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
	 * method that is called after the process completes
	 */
	private void done() {
		afterWorkComplete();
		formMode();
	}

	/**
	 * Constructs a layout with a single button that will start the process when clicked
	 * 
	 * @param parent
	 *            the layout that serves as the parent of the layout to construct
	 * @param caption
	 *            the caption of the button
	 */
	protected void constructSingleButtonLayout(Layout parent, String caption) {
		HorizontalLayout buttonBar = new DefaultHorizontalLayout(true, true, true);
		parent.addComponent(buttonBar);

		Button clearButton = new Button(caption);
		clearButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -652306482967612622L;

			@Override
			public void buttonClick(ClickEvent event) {
				startWork(null);
			}
		});
		buttonBar.addComponent(clearButton);
	}

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
	 * Estimates the total size of the process that is carried out
	 * 
	 * @param t
	 *            the object being processed (can be null)
	 * @return
	 */
	protected abstract int estimateSize(T t);

	protected OCSRuntimeException extractRuntimeException(Throwable t) {
		if (t instanceof OCSRuntimeException) {
			return (OCSRuntimeException) t;
		} else if (t.getCause() != null) {
			return extractRuntimeException(t.getCause());
		}
		return null;
	}

	/**
	 * Generic handling of an exception - tries to extract the OCSRuntimeException if possible
	 * 
	 * @param ex
	 */
	protected void handleException(Exception ex) {
		OCSRuntimeException r = extractRuntimeException(ex);
		if (r != null) {
			showNotification(r.getMessage(), Notification.Type.ERROR_MESSAGE);
		} else {
			showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

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
		ui.setPollInterval(-1);
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
	protected String getTitle() {
		// overwrite in subclass if needed
		return null;
	}

	/**
	 * Indicates whether the form is valid for processing. By default this method returns
	 * <code>true</code>. Override if needed and return false if the process cannot be started
	 * 
	 * @param t
	 *            the (optional) object that is being processed
	 * @return
	 */
	protected boolean isFormValid(T t) {
		return true;
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
			super.showNotifification(message, type);
		} finally {
			getUI().getSession().unlock();
		}
	}

	/**
	 * Start the actual work - this is the method that must be called in order to actually start the
	 * processing
	 * 
	 * @param t
	 *            the (optional) object that is being processed
	 */
	protected final void startWork(final T t) {
		if (isFormValid(t)) {
			if (ProgressMode.SIMPLE.equals(progressMode)) {
				// simply execute the process (without displaying any feedback)
				process(t, 0);
				done();
			} else {
				// switch to progress bar mode
				progressMode();

				// start a thread to update the progress
				try {
					final int estimatedSize = estimateSize(t);

					counter.set(0);
					ui.setPollInterval(POLL_INTERVAL);

					final ProgressBarUpdater updater = new ProgressBarUpdater(this, estimatedSize);

					// the thread that updates the progress bar
					Thread updateThread = new Thread(updater);
					updateThread.start();

					// the thread that performs the actual work
					Thread worker = new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								process(t, estimatedSize);
							} finally {
								updater.setStopped(true);

								final VaadinSession session = VaadinSession.getCurrent();
								session.lock();
								try {
									done();
								} finally {
									session.unlock();
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
						done();
					} finally {
						getUI().getSession().unlock();
					}
				}
			}
		}
	}

}
