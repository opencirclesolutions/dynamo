package com.ocs.dynamo.ui.view;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.BaseUI;
import com.ocs.dynamo.ui.navigator.CustomNavigator;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

public class BaseViewTest extends BaseMockitoTest {

	private static final String MODE = "mode";

	private static final String VIEW_ID = "view_id";

	private BaseView view;

	@Mock
	private EntityModelFactory modelFactory;

	@Mock
	private MessageService messageService;

	@Mock
	private BaseUI ui;

	@Mock
	private CustomNavigator navigator;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Mockito.when(ui.getNavigator()).thenReturn(navigator);

		view = new BaseView() {

			private static final long serialVersionUID = 8026762254250096616L;

			@Override
			public void enter(ViewChangeEvent event) {

			}
		};
		MockUtil.injectUI(view, ui);
		wireTestSubject(view);
	}

	@Test
	public void testGetScreenModeNull() {
		Assert.assertNull(view.getScreenMode());
	}

	@Test
	public void testGetViewMode() {
		Mockito.when(ui.getScreenMode()).thenReturn(MODE);
		Assert.assertEquals(MODE, view.getScreenMode());
	}

	@Test
	public void testNavigate() {
		view.navigate(VIEW_ID);
		Mockito.verify(navigator).navigateTo(VIEW_ID);
	}

	@Test
	public void testMessage() {
		view.message("key");
		Mockito.verify(messageService).getMessage("key");
	}
}
