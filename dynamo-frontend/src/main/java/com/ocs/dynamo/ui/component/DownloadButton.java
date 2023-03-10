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
import java.io.InputStream;
import java.util.function.Supplier;

import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.StreamResource;

import lombok.Getter;

/**
 * A button that starts a file download when clicked.
 *
 * @author bas.rutten
 */
public class DownloadButton extends HorizontalLayout {

	private static final long serialVersionUID = -7163648327567831406L;

	/**
	 * The actual link
	 */
	@Getter
	private final Anchor anchor;

	/**
	 * The button wrapped inside the link
	 */
	@Getter
	private final Button button;

	/**
	 * Supplier for creating the file name
	 */
	private final Supplier<String> createFileName;

	/**
	 * Supplier for creating the file content
	 */
	private final Supplier<InputStream> createContents;

	/**
	 * Constructor
	 * @param caption the caption to display on the button
	 * @param createContents code to carry out to retrieve the file contents
	 * @param createFileName code to carry out to construct the file name
	 */
	public DownloadButton(String caption, Supplier<InputStream> createContents, Supplier<String> createFileName) {
		this(caption, null, createContents, createFileName);
	}

	/**
	 * Constructor
	 * @param caption the caption to display on the button
	 * @param progressBar optional progress bar to keep track of the download process
	 * @param createContents code to carry out to retrieve the file contents
	 * @param createFileName code to carry out to construct the file name
	 */
	public DownloadButton(String caption, ProgressBar progressBar, Supplier<InputStream> createContents,
			Supplier<String> createFileName) {
		setMargin(false);
		this.createFileName = createFileName;
		this.createContents = createContents;

		anchor = new Anchor();
		update();

		button = new Button(caption, VaadinIcon.DOWNLOAD.create());
		button.addClassName(DynamoConstants.CSS_DOWNLOAD_BUTTON);
		if (progressBar != null) {
			button.addClickListener(event -> progressBar.setVisible(true));
		}

		anchor.getElement().setAttribute("download", true);
		anchor.addClassName(DynamoConstants.CSS_DOWNLOAD_BUTTON);
		anchor.add(button);
		add(anchor);
	}

	/**
	 * Updates the component after the contents that can be downloaded has changed
	 */
	public final void update() {
		anchor.setHref(new StreamResource(this.createFileName.get(), () -> {
			InputStream inputStream = this.createContents.get();
			if (inputStream == null) {
				return new ByteArrayInputStream(new byte[0]);
			}
			return inputStream;
		}));
	}

	@Override
	public boolean isEnabled() {
		return button.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
		if (enabled) {
			update();
		} else {
			anchor.removeHref();
		}
	}

}
