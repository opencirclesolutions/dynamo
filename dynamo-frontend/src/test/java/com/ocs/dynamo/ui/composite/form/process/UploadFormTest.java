package com.ocs.dynamo.ui.composite.form.process;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.form.process.ProgressForm.ProgressMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

public class UploadFormTest extends BaseMockitoTest {

    @Mock
    private UI ui;

    @Mock
    private VaadinSession session;

    @BeforeEach
    public void test() {
        MockVaadin.setup();
    }

    @Test
    public void testSimpleForm() throws IOException {
        UploadForm form = new UploadForm(ui, ProgressMode.SIMPLE, false) {

            private static final long serialVersionUID = -2866860896190814120L;

            @Override
            protected void process(byte[] t, int estimatedSize) {
                assertEquals(3, t.length);
            }

            @Override
            protected String getTitle() {
                return "Title";
            }

            @Override
            protected int estimateSize(byte[] t) {
                return 0;
            }
        };
        MockUtil.injectUI(form, ui);
        form.build();

        form.getUpload().setAutoUpload(true);
        form.getUpload().getReceiver().receiveUpload("test.txt", "text/plain");

    }
}
