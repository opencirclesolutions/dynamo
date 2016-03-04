package com.ocs.dynamo.ui.component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;

/**
 * An embedded component that is initialized using a byte array and that can be
 * used to display an image
 * 
 * @author bas.rutten
 */
@SuppressWarnings("serial")
public class DefaultEmbedded extends Embedded {

	private static final long serialVersionUID = 4282321844504066376L;

	/**
	 * Constructor
	 * 
	 * @param caption
	 *            the caption of the component
	 * @param bytes
	 *            the bytes that represent the
	 */
	public DefaultEmbedded(String caption, final byte[] bytes) {
		super(caption);
		if (bytes != null) {
			setSource(new StreamResource(new StreamSource() {

				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(bytes);
				}

			}, System.nanoTime() + ".png"));
		}
	}

}
