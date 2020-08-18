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

import java.io.IOException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

/**
 * A form that contains a file upload component and a progress bar
 * 
 * @author bas.rutten
 */
public abstract class UploadForm extends ProgressForm<byte[]> {

	private static final long serialVersionUID = -4717815709838453902L;

	/**
	 * Whether to display a cancel button
	 */
	private boolean showCancelButton;

	/**
	 * The upload component
	 */
	private Upload upload;

	/**
	 * The name of the uploaded file
	 */
	private String fileName;

	/**
	 * Constructor
	 * 
	 * @param progressMode     the desired progress mode
	 * @param showCancelButton whether to include a cancel button
	 */
	public UploadForm(UI ui, ProgressMode progressMode, boolean showCancelButton) {
		super(ui, progressMode);
		this.showCancelButton = showCancelButton;
	}

	/**
	 * The method that is executed after the cancel button is clicked
	 */
	protected void cancel() {
		// override in subclass if needed
	}

	/**
	 * Constructs the screen-specific form content
	 * 
	 * @param layout
	 */
	protected void doBuildForm(FormLayout layout) {
		// override in subclass
	}

	@Override
	protected void doBuildLayout(VerticalLayout main) {
		FormLayout form = new FormLayout();
		main.add(form);

		// add custom components
		doBuildForm(form);

		// add file upload field
		MemoryBuffer buffer = new MemoryBuffer();
		upload = new Upload(buffer);
		upload.addFinishedListener(event -> {
			this.fileName = event.getFileName();
			if (event.getContentLength() > 0L) {
				byte[] content = new byte[(int) event.getContentLength()];
				try {
					buffer.getInputStream().read(content);
					startWork(content);
				} catch (IOException e) {
					// do nothing
				}

			} else {
				showNotification(message("ocs.no.file.selected"));
			}
		});
		form.add(upload);

		if (showCancelButton) {
			Button cancelButton = new Button(message("ocs.cancel"));
			cancelButton.addClickListener(event -> cancel());
			main.add(cancelButton);
		}
	}

	public Upload getUpload() {
		return upload;
	}

	public String getFileName() {
		return fileName;
	}

}
