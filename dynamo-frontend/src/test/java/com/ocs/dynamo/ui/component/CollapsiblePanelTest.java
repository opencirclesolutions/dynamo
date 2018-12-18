package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.ui.VerticalLayout;

public class CollapsiblePanelTest {

	@Test
	public void testOpenClose() {
		CollapsiblePanel panel = new CollapsiblePanel("Caption", new VerticalLayout());
		panel.setOpen(true);
		Assert.assertTrue(panel.isOpen());

		panel.setOpen(false);
		Assert.assertFalse(panel.isOpen());
	}

	@Test
	public void testReplaceContent() {
		CollapsiblePanel panel = new CollapsiblePanel("Caption", new VerticalLayout());

		VerticalLayout v2 = new VerticalLayout();
		panel.setContent(v2);

		Assert.assertEquals(v2, panel.getContentWrapper().iterator().next());
	}
}
