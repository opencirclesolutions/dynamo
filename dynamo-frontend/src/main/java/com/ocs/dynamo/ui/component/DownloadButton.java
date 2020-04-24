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

import com.vaadin.flow.component.button.Button;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;

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
	private Anchor anchor;

	/**
	 * Button wrapped inside of link
	 */
	private Button button;

	/**
	 * Supplier for creating the file name
	 */
	private Supplier<String> createFileName;

	/**
	 * Supplier for creating the file content
	 */
	private Supplier<InputStream> createContent;

	/**
	 * Constructor
	 *
	 * @param caption the caption of the button
	 */
	public DownloadButton(String caption, Supplier<InputStream> createContent, Supplier<String> createFileName) {
		setMargin(false);
		this.createFileName = createFileName;
		this.createContent = createContent;

		anchor = new Anchor();
		update();

		button = new Button(caption, VaadinIcon.DOWNLOAD.create());
		button.addClassName("downloadButton");
		anchor.getElement().setAttribute("download", true);
		anchor.addClassName("downloadButton");
		anchor.add(button);
		add(anchor);
	}

	/**
	 * Updates the button after the content to download has been changed
	 */
	public final void update() {
		anchor.setHref(new StreamResource(this.createFileName.get(), () -> {
			InputStream inputStream = this.createContent.get();
			if (inputStream == null) {
				return new ByteArrayInputStream(new byte[0]);
			}
			return inputStream;
		}));
	}

	public Anchor getAnchor() {
		return anchor;
	}

	public Button getButton() {
		return button;
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
