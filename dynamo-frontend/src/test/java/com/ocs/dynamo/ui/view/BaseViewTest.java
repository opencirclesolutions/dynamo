package com.ocs.dynamo.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.UIHelper;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BaseViewTest extends BaseMockitoTest {

    private static final String MODE = "mode";

    private BaseView view;

    @Mock
    private EntityModelFactory modelFactory;

    @Mock
    private MessageService messageService;

    @Mock
    private UIHelper ui;

    @BeforeEach
    public void setUp() {

        view = new BaseView() {

            private static final long serialVersionUID = 3811868898666414611L;

            @Override
            protected void doInit(VerticalLayout layout) {

            }

        };
        ReflectionTestUtils.setField(view, "uiHelper", ui);
        ReflectionTestUtils.setField(view, "messageService", messageService);
    }

    @Test
    public void testGetScreenModeNull() {
        assertNull(view.getScreenMode());
    }

    @Test
    public void testGetViewMode() {
        when(ui.getScreenMode()).thenReturn(MODE);
        assertEquals(MODE, view.getScreenMode());

        view.clearScreenMode();
        verify(ui).setScreenMode(null);
    }

    @Test
    public void testMessage() {
        view.message("key");
        verify(messageService).getMessage(eq("key"), any(Locale.class));
    }

    @Test
    public void testMessageWithPars() {
        view.message("key", "bob");
        verify(messageService).getMessage(eq("key"), any(Locale.class), eq("bob"));
    }

    @Test
    public void testInit() {
        view.init();
        assertEquals(1, view.getComponentCount());
    }
}
