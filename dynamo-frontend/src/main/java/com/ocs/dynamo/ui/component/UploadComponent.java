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
package com.ocs.dynamo.ui.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

import lombok.Getter;
import lombok.Setter;

/**
 * A custom field that can be used to upload a file
 *
 * @author bas.rutten
 */
public class UploadComponent extends CustomField<byte[]> {

	private static final long serialVersionUID = -2254464483539583268L;

	@Setter
	@Getter
	private BiConsumer<String, byte[]> afterUploadCompleted;

	private final AttributeModel am;

	private Button clearButton;

	@Setter
	@Getter
	private Consumer<String> fileNameConsumer;

	private Image image;

	private final MessageService messageService;

	private Text text;

	private Upload upload;

	private byte[] value;

	public UploadComponent(AttributeModel am) {
		this.am = am;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		initContent();
	}

	private void afterUploadFinished(MemoryBuffer buffer, FinishedEvent event) {
		String extension = FilenameUtils.getExtension(event.getFileName());
		Set<String> allowedExtensions = am.getAllowedExtensions();
		if (allowedExtensions == null || allowedExtensions.isEmpty()
				|| (extension != null && allowedExtensions.contains(extension.toLowerCase()))) {

			byte[] content = new byte[(int) event.getContentLength()];
			try {
				buffer.getInputStream().read(content);
			} catch (IOException e) {
				// do nothing
			}
			setValue(content);

			if (fileNameConsumer != null) {
				fileNameConsumer.accept(event.getFileName());
			}

			if (afterUploadCompleted != null) {
				afterUploadCompleted.accept(event.getFileName(), content);
			}

		} else {
			VaadinUtils.showErrorNotification(
					messageService.getMessage("ocs.modelbasededitform.upload.format.invalid", VaadinUtils.getLocale()));
		}
		clearButton.setVisible(true);
	}

	@SuppressWarnings("deprecation")
	private void clearFilesList() {
		upload.getElement().executeJs("this.files=[]");
	}

	@Override
	protected byte[] generateModelValue() {
		return this.value;
	}

	@Override
	public byte[] getValue() {
		return this.value;
	}

	protected void initContent() {

		HorizontalLayout main = new HorizontalLayout();
		main.setSpacing(true);
	
		image = new Image();
		image.setClassName(DynamoConstants.CSS_IMAGE_PREVIEW);
		main.add(image);

		// for a LOB field, create an upload and an image
		// retrieve the current value
		if (am.isImage()) {
			image.setVisible(value != null);
			if (value != null) {
				image.setSrc(new StreamResource(System.nanoTime() + ".png", () -> new ByteArrayInputStream(value)));
			}
			main.add(image);
		} else {
			text = new Text(messageService.getMessage("ocs.no.preview.available", VaadinUtils.getLocale()));
			main.add(text);
		}

		MemoryBuffer buffer = new MemoryBuffer();
		upload = new Upload(buffer);

		if (am.getAllowedExtensions() != null && !am.getAllowedExtensions().isEmpty()) {
			Set<String> extensions = am.getAllowedExtensions().stream().map(s -> "." + s).collect(Collectors.toSet());
			upload.setAcceptedFileTypes(extensions.toArray(new String[0]));
		}

		upload.addFinishedListener(event -> afterUploadFinished(buffer, event));

		// clear content and file name after file upload is removed
		upload.getElement().addEventListener("file-remove", event -> {
			image.setVisible(false);
			if (am.getFileNameProperty() != null) {
				setValue(null);

				if (fileNameConsumer != null) {
					fileNameConsumer.accept(null);
				}
			}
			clearButton.setVisible(false);
		});

		// if there is an initial value, provide a clear button
		main.add(upload);
		clearButton = new Button(messageService.getMessage("ocs.clear", VaadinUtils.getLocale()));
		clearButton.addClickListener(event -> {
			setValue(null);
			clearFilesList();

			image.setVisible(false);
			if (am.getFileNameProperty() != null) {

				if (fileNameConsumer != null) {
					fileNameConsumer.accept(null);
				}
			}
			// clear button is no longer needed since the component itself can be used
			clearButton.setVisible(false);

		});
		main.add(clearButton);
		clearButton.setVisible(value != null);

		setLabel(am.getDisplayName(VaadinUtils.getLocale()));
		add(main);
	}

	@Override
	protected void setPresentationValue(byte[] newPresentationValue) {
		this.value = newPresentationValue;
		if (upload != null) {
			clearFilesList();
		}
		showValue();
	}

	@Override
	public void setValue(byte[] value) {
		this.value = value;
		super.setValue(value);

		showValue();
	}

	private void showValue() {
		if (am.isImage()) {
			image.setVisible(value != null);
			if (value != null) {
				image.setSrc(new StreamResource(System.nanoTime() + ".png", () -> new ByteArrayInputStream(value)));
			}
		} else {
			text.setText(messageService.getMessage("ocs.no.preview.available", VaadinUtils.getLocale()));
		}
		clearButton.setVisible(value != null);
	}

}
