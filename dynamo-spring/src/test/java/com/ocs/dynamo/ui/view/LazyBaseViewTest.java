package com.ocs.dynamo.ui.view;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class LazyBaseViewTest extends BaseMockitoTest {

	private LazyBaseView view;

	private VerticalLayout layout;

	private int buildCount;

	private int refreshCount;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		view = new LazyBaseView() {

			private static final long serialVersionUID = 84558218243299300L;

			@Override
			protected Component build() {
				buildCount++;
				layout = new VerticalLayout();
				return layout;
			}

			@Override
			protected void refresh() {
				refreshCount++;
			}
		};
	}

	/**
	 * Enter the view multiple times and test
	 */
	@Test
	public void testConstructJustOnce() {
		buildCount = 0;
		view.enter(null);
		view.enter(null);
		view.enter(null);

		Assert.assertEquals(1, buildCount);
		Assert.assertNotNull(layout);
		
		Assert.assertEquals(2, refreshCount);
	}
}
