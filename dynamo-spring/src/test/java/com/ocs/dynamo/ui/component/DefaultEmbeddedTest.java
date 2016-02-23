package com.ocs.dynamo.ui.component;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.server.StreamResource;

public class DefaultEmbeddedTest {

	@Test
	public void test() {
		DefaultEmbedded embedded = new DefaultEmbedded("Caption", new byte[] { 1, 2, 3, 4 });

		Assert.assertTrue(embedded.getSource() instanceof StreamResource);
		StreamResource resource = (StreamResource) embedded.getSource();
		Assert.assertTrue(resource.getStreamSource().getStream() instanceof ByteArrayInputStream);
	}

	@Test
	public void testEmpty() {
		DefaultEmbedded embedded = new DefaultEmbedded("Caption", null);
		Assert.assertNull(embedded.getSource());
	}
}
