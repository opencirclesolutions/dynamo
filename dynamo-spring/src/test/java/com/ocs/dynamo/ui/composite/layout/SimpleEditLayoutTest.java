package com.ocs.dynamo.ui.composite.layout;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;

public class SimpleEditLayoutTest extends BaseIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private TestEntityService testEntityService;

    private TestEntity e1;

    private TestEntity e2;

    @Before
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);
    }

    @Test
    public void testSimpleEditLayout() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, new FormOptions());

        Map<String, Filter> ff = new HashMap<>();
        ff.put("testEntities", new Compare.Equal("name", "Harry"));

        layout.setFieldFilters(ff);
        layout.build();

        Assert.assertNotNull(layout.getEditForm());

        Assert.assertEquals(1, layout.getFieldFilters().size());
        Assert.assertEquals("Bob", layout.getEditForm().getField("name").getValue());

        // check that the screen is not in view mode
        Assert.assertFalse(layout.getEditForm().isViewMode());
        Assert.assertTrue(layout.getEditForm().getSaveButtons().get(0).isVisible());

        layout.setEntity(e2);
        Assert.assertEquals("Harry", layout.getEditForm().getField("name").getValue());
    }

    @Test
    public void testSimpleEditLayout_ViewMode() {
        SimpleEditLayout<Integer, TestEntity> layout = createLayout(e1, new FormOptions().setOpenInViewMode(true));
        layout.build();

        Assert.assertNotNull(layout.getEditForm());

        // check that the screen is in view mode and no save buttons are visible
        Assert.assertTrue(layout.getEditForm().isViewMode());
        Assert.assertTrue(layout.getEditForm().getSaveButtons().isEmpty());
    }

    private SimpleEditLayout<Integer, TestEntity> createLayout(TestEntity entity, FormOptions fo) {
        return new SimpleEditLayout<Integer, TestEntity>(entity, testEntityService,
                entityModelFactory.getModel(TestEntity.class), fo);

    }
}
