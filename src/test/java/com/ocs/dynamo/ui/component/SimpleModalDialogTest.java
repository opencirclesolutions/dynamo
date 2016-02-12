package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.ui.composite.dialog.SimpleModalDialog;
import com.vaadin.ui.Layout;

public class SimpleModalDialogTest {

	@Test
	public void testShowCancelButton() {
		SimpleModalDialog dialog = new SimpleModalDialog(true) {

			private static final long serialVersionUID = 87021849566418546L;

			@Override
			protected void doBuild(Layout parent) {

			}

			@Override
			protected String getTitle() {
				return "Title";
			}

		};
		dialog.build();

		Assert.assertNotNull(dialog.getOkButton());
		Assert.assertNotNull(dialog.getCancelButton());
		Assert.assertTrue(dialog.getCancelButton().isVisible());

		dialog.getOkButton().click();
		dialog.getCancelButton().click();

	}

	@Test
	public void testHideCancelButton() {
		SimpleModalDialog dialog = new SimpleModalDialog(false) {

			private static final long serialVersionUID = 87021849566418546L;

			@Override
			protected void doBuild(Layout parent) {

			}

			@Override
			protected String getTitle() {
				return "Title";
			}

		};
		dialog.build();

		Assert.assertNotNull(dialog.getOkButton());
		Assert.assertNotNull(dialog.getCancelButton());
		Assert.assertFalse(dialog.getCancelButton().isVisible());
	}
}
