package com.ocs.dynamo.functional.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.Lists;
import com.ocs.dynamo.functional.domain.Country;
import com.ocs.dynamo.functional.domain.Domain;
import com.ocs.dynamo.functional.domain.Region;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.layout.BaseSplitLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

/**
 * Test for the MultiDomainEditLayout
 * 
 * @author bas.rutten
 *
 */
@SpringBootTest(classes = DomainFrontendIntegrationTestConfig.class)
public class MultiDomainEditLayoutTest extends FrontendIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testCreate() {

        FormOptions fo = new FormOptions().setShowQuickSearchField(true).setShowRemoveButton(true);
        MultiDomainEditLayout layout = new MultiDomainEditLayout(fo, Lists.newArrayList(Country.class, Region.class));
        layout.addEntityModelOverride(Region.class, "CustomRegion");
        layout.build();

        // check that first domain class is selected by default
        assertEquals(2, layout.getDomainClasses().size());
        assertEquals(Country.class, layout.getSelectedDomain());
        assertTrue(layout.isDeleteAllowed(Country.class));

        layout.selectDomain(Region.class);

        BaseSplitLayout<?, ?> splitLayout = layout.getSplitLayout();
        assertNotNull(splitLayout);
        splitLayout.build();

        // adding is possible
        assertNotNull(splitLayout.getAddButton());
        assertTrue(splitLayout.getAddButton().isVisible());

        // test reload
        splitLayout.reload();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateInReadOnly() {
        FormOptions fo = new FormOptions();
        MultiDomainEditLayout layout = new MultiDomainEditLayout(fo, Lists.newArrayList(Country.class, Region.class)) {

            private static final long serialVersionUID = -2364409278522556475L;

            @Override
            protected boolean isEditAllowed() {
                return false;
            }

            @Override
            protected void postProcessButtonBar(FlexLayout buttonBar) {
                Button button = new Button("testButton");
                buttonBar.add(button);
                registerComponent(button);
            }
        };
        layout.build();

        BaseSplitLayout<?, ?> splitLayout = layout.getSplitLayout();
        splitLayout.build();

        // adding is not possible
        assertNotNull(splitLayout.getAddButton());
        assertFalse(splitLayout.getAddButton().isVisible());

        // test the reload method
        layout.reload();
    }

    /**
     * Test what happens if there is no service class defined
     */
    // @Test(expected = MethodException.class)
    public void testCreateServiceMissing() {
        List<Class<? extends Domain>> list = new ArrayList<>();
        list.add(TestDomain.class);

        FormOptions fo = new FormOptions();
        MultiDomainEditLayout layout = new MultiDomainEditLayout(fo, list);
        layout.build();
    }

    private class TestDomain extends Domain {

        private static final long serialVersionUID = -204959303189799878L;

    }
}
