package com.ocs.dynamo.ui.composite.form.process;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.form.process.ProgressForm.ProgressMode;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

public class UploadFormTest extends BaseMockitoTest {

    private boolean started = false;

    @Mock
    private UI ui;

    @Mock
    private VaadinSession session;

    @Test
    public void testSimpleForm() throws IOException {
        UploadForm form = new UploadForm(ui, ProgressMode.SIMPLE, false) {

            private static final long serialVersionUID = -2866860896190814120L;

            @Override
            protected void process(byte[] t, int estimatedSize) {
                Assert.assertEquals(3, t.length);
                started = true;
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

        OutputStream stream = form.getUpload().getReceiver().receiveUpload("test.txt", "text/plain");
        stream.write(new byte[] { 1, 2, 3 });

        SucceededListener listener = (SucceededListener) form.getUpload().getReceiver();
        listener.uploadSucceeded(new SucceededEvent(form.getUpload(), "test.txt", "text/plain", 3));

        Assert.assertTrue(started);
    }
}
