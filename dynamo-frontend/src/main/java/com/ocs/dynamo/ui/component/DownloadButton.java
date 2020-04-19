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

import com.vaadin.flow.component.html.Anchor;
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
		setMargin(true);
		this.createFileName = createFileName;
		this.createContent = createContent;

		anchor = new Anchor(new StreamResource(createFileName.get(), () -> {
			InputStream inputStream = createContent.get();
			if (inputStream == null) {
				return new ByteArrayInputStream(new byte[0]);
			}
			return inputStream;
		}), caption);
		anchor.getElement().setAttribute("download", true);
		anchor.addClassName("donwloadButton");
		add(anchor);
	}

	/**
	 * Updates the button after the content to download has been changed
	 */
	public void update() {
		anchor.setHref(new StreamResource(createFileName.get(), () -> createContent.get()));
	}

	public Anchor getAnchor() {
		return anchor;
	}

}
