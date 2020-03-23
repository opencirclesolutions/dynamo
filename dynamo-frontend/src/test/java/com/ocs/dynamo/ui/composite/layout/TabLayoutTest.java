package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.Reloadable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TabLayoutTest extends FrontendIntegrationTest {

    @Inject
    private TestEntityService testEntityService;

    private TestEntity e1;

    private TestEntity e2;

    private boolean reloaded;

    @BeforeEach
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);
    }

    private class MyLayout extends HorizontalLayout implements Reloadable {

        private static final long serialVersionUID = -8386385689879382158L;

        @Override
        public void reload() {
            reloaded = true;
        }

    }

    @Test
    public void test() {
        TabLayout<Integer, TestEntity> layout = new TabLayout<Integer, TestEntity>(e1) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component initTab(int index) {
                switch (index) {
                case 0:
                    return new MyLayout();
                case 1:
                    return new HorizontalLayout();
                default:
                    return null;
                }
            }

            @Override
            protected String[] getTabCaptions() {
                return new String[] { "tab1", "tab2" };
            }

            @Override
            protected String createTitle() {
                return "Test Tab Layout";
            }

            @Override
            protected Icon getIconForTab(int index) {
                return null;
            }

        };
        layout.build();

        assertTrue(layout.getComponentAt(0) instanceof MyLayout);

        // second tab has not been created yet
        assertTrue(layout.getComponentAt(1) instanceof VerticalLayout);

        // select the second tab, this will lazily create it
        layout.selectTab(1);
        assertTrue(layout.getComponentAt(1) instanceof HorizontalLayout);

        // select first tab again and trigger a reload
        reloaded = false;
        layout.selectTab(0);
        assertTrue(reloaded);

        // second tab is not reloadable
        reloaded = false;
        layout.selectTab(1);
        assertFalse(reloaded);

    }
}
