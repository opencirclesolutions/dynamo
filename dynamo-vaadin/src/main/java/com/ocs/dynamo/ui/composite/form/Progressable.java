package com.ocs.dynamo.ui.composite.form;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;

/**
 * Interface for a component that supports the display of a progress bar. This
 * interface is used to be passed to an instance of ProgressBarUpdate
 * 
 * @author bas.rutten
 */
public interface Progressable {

	/**
	 * Estimates the current progress of the process
	 * 
	 * @return
	 */
	public int estimateCurrentProgress();

	/**
	 * Retrieves the progress bar
	 * 
	 * @return
	 */
	public ProgressBar getProgressBar();

	/**
	 * Retrieves the status label
	 * 
	 * @return
	 */
	public Label getStatusLabel();
}
