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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A form that displays of a progress bar while some time consuming process is
 * taking place
 * 
 * @author bas.rutten
 * @param <T> the type of the object that is being processed. This can usually
 *            be "Object" but can e.g. be more specific in case of a file upload
 *            you can provide a byte array.
 */
@Slf4j
public class ProgressForm<T> extends BaseCustomComponent implements Progressable {

	public enum ProgressMode {
		PROGRESSBAR, SIMPLE;
	}

	/**
	 * Polling interval in milliseconds
	 */
	public static final int POLL_INTERVAL = 500;

	private static final long serialVersionUID = -4717815709838453902L;

	/**
	 * Callback method that is carried out after the form mode in selected
	 */
	@Getter
	@Setter
	private Runnable afterFormModeEntered;

	/**
	 * Callback method that is carried out after the work is complete
	 */
	@Getter
	@Setter
	private Consumer<Boolean> afterWorkComplete;

	/**
	 * Callback method that is carried out the build the actual layout
	 */
	@Getter
	@Setter
	private Consumer<VerticalLayout> buildMainLayout = layout -> {
	};

	@Getter
	private ProgressCounter counter = new DefaultProgressCounter();

	/**
	 * Callback method that is used to estimate the total size of the data set to
	 * process
	 */
	@Getter
	@Setter
	private Function<T, Integer> estimateSize = t -> 0;

	/**
	 * Callback method that is carried out to validate whether the form input is
	 * valid
	 */
	@Getter
	@Setter
	private Predicate<T> isFormValid = t -> true;

	private VerticalLayout mainLayout;

	/**
	 * Callback method that is called to do the actual processing
	 */
	@Getter
	@Setter
	private BiConsumer<T, Integer> process;

	@Getter
	private ProgressBar progressBar;

	private VerticalLayout progressLayout;

	private ProgressMode progressMode;

	/**
	 * The label that is used to display the status (current completion percentage)
	 */
	@Getter
	private Text statusLabel;

	@Getter
	@Setter
	private String title;

	@Getter
	private UI ui;

	/**
	 * Constructor
	 * 
	 * @param ui           the current UI
	 * @param progressMode indicates the mode of the screen (and whether the
	 *                     progress bar will be displayed)
	 */
	public ProgressForm(UI ui, ProgressMode progressMode) {
		this.ui = ui;
		this.progressMode = progressMode;
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
	 * Method that is called after the job completes
	 */
	private void done(boolean exceptionOccurred) {
		if (afterWorkComplete != null) {
			afterWorkComplete.accept(exceptionOccurred);
		}
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
	 * Executes the process, with periodic polling to determine the progress
	 * 
	 * @param input the input for the processing
	 */
	private void executeProcessWithPolling(T input) {
		// switch to progress bar mode
		progressMode();

		// start a thread to update the progress
		try {
			int estimatedSize = estimateSize.apply(input);

			counter.reset();
			ui.setPollInterval(POLL_INTERVAL);

			ProgressBarUpdater updater = new ProgressBarUpdater(ui, this, estimatedSize);

			// the thread that updates the progress bar
			Thread updateThread = new Thread(updater);
			updateThread.start();

			// the thread that performs the actual work
			Thread worker = new Thread(() -> {
				try {
					process.accept(input, estimatedSize);
				} finally {
					updater.setStopped(true);
					signalDone(false);
				}
			});
			worker.start();
		} catch (RuntimeException ex) {
			log.error(ex.getMessage(), ex);
			// exception during size estimation
			showNotification(ex.getMessage());
			signalDone(true);
		}
	}

	/**
	 * Executes the process without polling
	 * 
	 * @param input the input
	 */
	private void executeSimpleProcess(T input) {
		try {
			process.accept(input, 0);
			done(false);
		} catch (RuntimeException ex) {
			log.error(ex.getMessage(), ex);
			// exception during size estimation
			showNotification(ex.getMessage());
			signalDone(true);
		}
	}

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
			if (buildMainLayout != null) {
				buildMainLayout.accept(mainLayout);
			}
		}

		// disable polling
		ui.setPollInterval(-1);
		removeAll();
		add(mainLayout);
		if (afterFormModeEntered != null) {
			afterFormModeEntered.run();
		}
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

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	/**
	 * Displays a layout that contains a progress bar and a status label
	 */
	private void progressMode() {
		// lazily build the layout
		if (progressLayout == null) {
			progressLayout = new DefaultVerticalLayout(true, true);

			progressBar = new ProgressBar();
			progressBar.setHeight("20px");
			progressLayout.add(progressBar);

			statusLabel = new Text("");
			progressLayout.add(statusLabel);
		}
		progressBar.setValue(0.0f);
		statusLabel.setText("");

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
	protected final void startWork(T input) {
		if (isFormValid == null || isFormValid.test(input)) {

			if (process == null) {
				throw new IllegalStateException("You must define the process. Please use the setProcess method");
			}

			if (ProgressMode.SIMPLE.equals(progressMode)) {
				// simply execute the process (without displaying any feedback)
				executeSimpleProcess(input);
			} else {
				executeProcessWithPolling(input);
			}
		}
	}

}
