package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

public class FlexibleSearchLayoutTest extends FrontendIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private TestEntityService testEntityService;

    private TestEntity e1;

    @BeforeEach
    public void setup() {
        MockVaadin.setup();
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        TestEntity e2 = new TestEntity("Kevin", 12L);
        e2 = testEntityService.save(e2);

        TestEntity e3 = new TestEntity("Stewart", 13L);
        e3 = testEntityService.save(e3);
    }

    @Test
    public void testFlexibleSearchLayout() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();

        assertTrue(layout.getAddButton().isVisible());
        assertTrue(layout.getEditButton().isVisible());
        assertFalse(layout.getRemoveButton().isVisible());

        assertEquals("name", layout.getSortOrders().get(0).getSorted());
        assertEquals(SortDirection.ASCENDING, layout.getSortOrders().get(0).getDirection());

        TestEntity entity = layout.createEntity();
        assertNotNull(entity);

        layout.getGridWrapper().forceSearch();
        assertEquals(3, layout.getGridWrapper().getDataProviderSize());

        layout.select(e1);
    }

    @Test
    public void testFlexibleSearchLayout_AddButton() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();

        // click the add button and verify that a new item is added
        layout.getAddButton().click();
        assertNotNull(layout.getSelectedItem());
    }

    @Test
    public void testFlexibleSearchLayout_EditButton() {
        FormOptions options = new FormOptions().setEditAllowed(true);

        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(options);
        layout.build();

        assertTrue(layout.getEditButton().isVisible());

        // click the add button and verify that a new item is added
        layout.setSelectedItem(e1);
        layout.getEditButton().click();
        assertEquals(e1, layout.getSelectedItem());
    }

    /**
     * Test the user of a filter
     */
    @Test
    public void testFlexibleSearchLayout_Filter() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());

        List<SerializablePredicate<TestEntity>> filters = new ArrayList<>();
        filters.add(new EqualsPredicate<TestEntity>("name", "Bob"));

        layout.setDefaultFilters(filters);
        layout.build();
        layout.getGridWrapper().forceSearch();

        assertEquals("name", layout.getSortOrders().get(0).getSorted());
        assertEquals(SortDirection.ASCENDING, layout.getSortOrders().get(0).getDirection());

        assertEquals(1, layout.getGridWrapper().getDataProviderSize());
    }

    /**
     * Test the selection of an item (single item)
     */
    @Test
    public void testFlexibleSearchLayout_Select() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();

        layout.select(e1);
        assertEquals(e1, layout.getSelectedItem());
    }

    @Test
    public void testFlexibleSearchLayout_SelectCollection() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();
        layout.select(e1);
        assertEquals(e1, layout.getSelectedItem());
    }

    /**
     * Test setting a pre-defined search value
     */
    @Test
    public void testFlexibleSearchLayout_setSearchValue() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();

        layout.setSearchValue("age", "13", "15");
        layout.search();
        layout.getGridWrapper().forceSearch();

        // verify that this creates a filter
        assertTrue(layout.getSearchForm().hasFilter(entityModelFactory.getModel(TestEntity.class).getAttributeModel("age")));
        assertEquals(1, layout.getGridWrapper().getDataProviderSize());
    }

    /**
     * Test the functionality for making search fields required
     */
    @Test
    public void testFlexibleSearchLayout_Mandatory() {
        FlexibleSearchLayout<Integer, TestEntity> layout = createLayout("TestEntitySearchRequired", new FormOptions());
        layout.build();

        assertFalse(layout.getSearchForm().isSearchAllowed());
        layout.setSearchValue("name", "abc");
        assertTrue(layout.getSearchForm().isSearchAllowed());
    }

    private FlexibleSearchLayout<Integer, TestEntity> createLayout(FormOptions fo) {
        return new FlexibleSearchLayout<>(testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.ID_BASED, fo,
                new SortOrder<String>("name", SortDirection.ASCENDING));

    }

    private FlexibleSearchLayout<Integer, TestEntity> createLayout(String reference, FormOptions fo) {
        return new FlexibleSearchLayout<>(testEntityService, entityModelFactory.getModel(reference, TestEntity.class), QueryType.ID_BASED,
                fo, new SortOrder<String>("name", SortDirection.ASCENDING));

    }
}
