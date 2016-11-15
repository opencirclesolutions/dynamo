package com.ocs.dynamo.ui.composite.form;

import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.form.ProgressForm.ProgressMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.ui.UI;

public class UploadFormTest extends BaseMockitoTest {

    @Mock
    private UI ui;

    @Test
    public void testSimpleForm() {
        UploadForm form = new UploadForm(ProgressMode.SIMPLE, ScreenMode.HORIZONTAL, false) {

            private static final long serialVersionUID = -2866860896190814120L;

            @Override
            protected void process(byte[] t, int estimatedSize) {
            }

            @Override
            protected String getTitle() {
                return "Title";
            }

            @Override
            protected int estimateSize(byte[] t) {
                // TODO Auto-generated method stub
                return 0;
            }
        };
        MockUtil.injectUI(form, ui);
        form.build();
    }
}
