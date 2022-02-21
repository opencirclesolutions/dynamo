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
import java.util.function.Consumer;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import elemental.json.Json;
import lombok.Getter;
import lombok.Setter;

/**
 * A form that contains a file upload component and a progress bar
 * 
 * @author bas.rutten
 */
public class UploadForm extends ProgressForm<byte[]> {

	private static final long serialVersionUID = -4717815709838453902L;

	@Getter
	private boolean showCancelButton;

	@Getter
	private Upload upload;

	@Getter
	private String fileName;

	@Getter
	@Setter
	private Consumer<FormLayout> buildForm;

	@Getter
	@Setter
	private Runnable onCancel;

	/**
	 * Constructor
	 * 
	 * @param progressMode     the desired progress mode
	 * @param showCancelButton whether to include a cancel button
	 */
	public UploadForm(UI ui, ProgressMode progressMode, boolean showCancelButton) {
		super(ui, progressMode);
		this.showCancelButton = showCancelButton;
		setBuildMain(main -> {
			FormLayout form = new FormLayout();
			main.add(form);

			// add custom components
			if (buildForm != null) {
				buildForm.accept(form);
			}

			// add file upload field
			MemoryBuffer buffer = new MemoryBuffer();
			upload = new Upload(buffer);
			upload.setClassName("dynamoUpload");
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
				cancelButton.addClickListener(event -> {
					if (onCancel != null) {
						onCancel.run();
					}
				});
				main.add(cancelButton);
			}
		});
	}

	/**
	 * Shows an error after file upload and clears the upload component
	 * 
	 * @param message the message to show
	 */
	protected void showErrorAndClear(String message) {
		VaadinUtils.showErrorNotification(message);
		getUpload().getElement().setPropertyJson("files", Json.createArray());
	}

}
