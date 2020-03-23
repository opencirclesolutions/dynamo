/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.dialog.SimpleModalDialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SimpleModalDialogTest extends FrontendIntegrationTest {

    @Test
    public void testShowCancelButton() {
        SimpleModalDialog dialog = new SimpleModalDialog(true) {

            private static final long serialVersionUID = 87021849566418546L;

            @Override
            protected void doBuild(VerticalLayout parent) {

            }

            @Override
            protected String getTitle() {
                return "Title";
            }

        };
        dialog.build();

        assertNotNull(dialog.getOkButton());
        assertNotNull(dialog.getCancelButton());
        assertTrue(dialog.getCancelButton().isVisible());

        dialog.getOkButton().click();
        dialog.getCancelButton().click();

    }

    @Test
    public void testHideCancelButton() {
        SimpleModalDialog dialog = new SimpleModalDialog(false) {

            private static final long serialVersionUID = 87021849566418546L;

            @Override
            protected void doBuild(VerticalLayout parent) {

            }

            @Override
            protected String getTitle() {
                return "Title";
            }

        };
        dialog.build();

        assertNotNull(dialog.getOkButton());
        assertNotNull(dialog.getCancelButton());
        assertFalse(dialog.getCancelButton().isVisible());
    }
}
