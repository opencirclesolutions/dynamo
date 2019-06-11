package com.ocs.dynamo.ui.view;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Before
    public void setUp() {
        Mockito.when(ui.getNavigator()).thenReturn(navigator);

        view = new BaseView() {

            private static final long serialVersionUID = 8026762254250096616L;

            @Override
            public void enter(ViewChangeEvent event) {

            }
        };
        MockUtil.injectUI(view, ui);
        ReflectionTestUtils.setField(view, "messageService", messageService);

    }

    @Test
    public void testGetScreenModeNull() {
        Assert.assertNull(view.getScreenMode());
    }

    @Test
    public void testGetViewMode() {
        Mockito.when(ui.getScreenMode()).thenReturn(MODE);
        Assert.assertEquals(MODE, view.getScreenMode());

        view.clearScreenMode();
        Mockito.verify(ui).setScreenMode(null);
    }

    @Test
    public void testNavigate() {
        view.navigate(VIEW_ID);
        Mockito.verify(navigator).navigateTo(VIEW_ID);
    }

    @Test
    public void testMessage() {
        view.message("key");
        Mockito.verify(messageService).getMessage(Mockito.eq("key"), Mockito.any(Locale.class));
    }

    @Test
    public void testMessageWithPars() {
        view.message("key", "bob");
        Mockito.verify(messageService).getMessage(Mockito.eq("key"), Mockito.any(Locale.class), Mockito.eq("bob"));
    }
}
