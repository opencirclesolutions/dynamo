package com.ocs.dynamo.ui.composite.layout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.ocs.dynamo.domain.CascadeEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.CascadeEntityService;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

public class SimpleSearchLayoutTest extends FrontendIntegrationTest {

    private static Routes routes;

    @BeforeClass
    public static void createRoutes() {
        // initialize routes only once, to avoid view auto-detection before every test
        // and to speed up the tests
        routes = new Routes().autoDiscoverViews("com.ocs.dynamo");
    }
    
    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private TestEntityService testEntityService;

    @Inject
    private CascadeEntityService cascadeEntityService;

    private TestEntity e1;

    private TestEntity e2;

    private TestEntity e3;

    private boolean detailsTabCreated = false;

    @Before
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Kevin", 12L);
        e2 = testEntityService.save(e2);

        e3 = new TestEntity("Stewart", 14L);
        e3 = testEntityService.save(e3);
        
        MockVaadin.setup(routes);
    }

    @Test
    public void testSimpleSearchLayout() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setShowIterationButtons(true));
        layout.build();

        Assert.assertTrue(layout.getAddButton().isVisible());
        Assert.assertTrue(layout.getEditButton().isVisible());
        Assert.assertFalse(layout.getRemoveButton().isVisible());

        Assert.assertEquals("name", layout.getSortOrders().get(0).getSorted());

        TestEntity entity = layout.createEntity();
        Assert.assertNotNull(entity);

        layout.getGridWrapper().forceSearch();
        Assert.assertEquals(3, layout.getGridWrapper().getDataProviderSize());

        layout.select(e3);
        layout.detailsMode(e3);

        // click the next button
        Assert.assertEquals(2, layout.getEditForm().getNextButtons().size());

        layout.getEditForm().getNextButtons().iterator().next().click();
        Assert.assertEquals(e2, layout.getEditForm().getEntity());
        Assert.assertTrue(layout.hasPrevEntity());
        Assert.assertTrue(layout.hasNextEntity());

        // click the previous button
        Assert.assertEquals(2, layout.getEditForm().getPreviousButtons().size());
        layout.getEditForm().getPreviousButtons().iterator().next().click();
        Assert.assertEquals(e3, layout.getEditForm().getEntity());
    }

    /**
     * Check that a complex details tab is created when requested
     */
    @Test
    public void testSimpleSearchLayout_ComplexDetailMode() {
        detailsTabCreated = false;
        SimpleSearchLayout<Integer, TestEntity> layout = new SimpleSearchLayout<Integer, TestEntity>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), QueryType.ID_BASED, new FormOptions().setComplexDetailsMode(true),
                new SortOrder<String>("id", SortDirection.ASCENDING)) {

            private static final long serialVersionUID = -5529138385460474211L;

            @Override
            protected String[] getDetailModeTabCaptions() {
                return new String[] { "title 1" };
            }

            @Override
            protected Component constructComplexDetailModeTab(TestEntity entity, int index, FormOptions fo, boolean newEntity) {
                detailsTabCreated = true;
                return new VerticalLayout();
            }
        };
        layout.build();

        // click add button, verify that we just get a simple edit screen and not
        // the tab layout
        layout.getAddButton().click();
        Assert.assertTrue(detailsTabCreated);

        // select the item and verify that the proper details tab is created
        layout.setSelectedItem(e1);
        layout.getEditButton().click();
        Assert.assertTrue(detailsTabCreated);

        // select another item
        layout.searchMode();
        layout.setSelectedItem(e2);
        layout.getEditButton().click();

    }

    @Test
    public void testSimpleSearchLayout_AddButton1() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setOpenInViewMode(true));
        layout.build();

        // click the add button and verify that a new item is added
        layout.getAddButton().click();
        Assert.assertNotNull(layout.getSelectedItem());

        Assert.assertFalse(layout.isInSearchMode());
        Assert.assertFalse(layout.getEditForm().isViewMode());

        layout.getEditForm().getSaveButtons().get(0).click();
        Assert.assertFalse(layout.isInSearchMode());
    }

    @Test
    public void testSimpleSearchLayout_AddButton2() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setOpenInViewMode(false));
        layout.build();

        // click the add button and verify that a new item is added
        layout.getAddButton().click();
        Assert.assertNotNull(layout.getSelectedItem());

        Assert.assertFalse(layout.isInSearchMode());
        Assert.assertFalse(layout.getEditForm().isViewMode());

        layout.getEditForm().getSaveButtons().get(0).click();
        Assert.assertFalse(layout.isInSearchMode());
    }

    @Test
    @Ignore
    public void testSimpleSearchLayout_EditButton() {
        FormOptions options = new FormOptions();
        options.setEditAllowed(true).setOpenInViewMode(false);

        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(options);
        layout.build();

        Assert.assertTrue(layout.getEditButton().isVisible());

        // select an item and edit
        layout.setSelectedItem(e1);
        layout.getEditButton().click();
        Assert.assertEquals(e1, layout.getSelectedItem());
        Assert.assertFalse(layout.getEditForm().isViewMode());

        // save changes and check that we go back to search mode
        layout.getEditForm().getSaveButtons().get(0).click();
        Assert.assertTrue(layout.isInSearchMode());
    }

    @Test
    public void testSimpleSearchLayout_EditButton2() {
        FormOptions options = new FormOptions();
        options.setEditAllowed(true).setOpenInViewMode(true);

        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(options);
        layout.build();

        Assert.assertTrue(layout.getEditButton().isVisible());

        // select an item and edit
        layout.setSelectedItem(e1);
        layout.getEditButton().click();
        Assert.assertEquals(e1, layout.getSelectedItem());
        Assert.assertTrue(layout.getEditForm().isViewMode());
        layout.getEditForm().getEditButtons().get(0).click();
        Assert.assertFalse(layout.getEditForm().isViewMode());

        // save changes and check that are back on the detail screen is view mode
        layout.getEditForm().getSaveButtons().get(0).click();
        Assert.assertTrue(layout.getEditForm().isViewMode());
        Assert.assertFalse(layout.isInSearchMode());
    }

    /**
     * Test the use of a filter
     */
    @Test
    public void testSimpleSearchLayout_Filter() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());

        List<SerializablePredicate<TestEntity>> filters = new ArrayList<>();
        filters.add(new EqualsPredicate<>("name", "Bob"));

        layout.setDefaultFilters(filters);
        layout.build();
        layout.getGridWrapper().forceSearch();

        Assert.assertEquals("name", layout.getSortOrders().get(0).getSorted());
        Assert.assertEquals(SortDirection.ASCENDING, layout.getSortOrders().get(0).getDirection());
        Assert.assertEquals(1, layout.getGridWrapper().getDataProviderSize());
    }

    /**
     * Test the selection of an item (single item)
     */
    @Test
    public void testSimpleSearchLayout_Select() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();
        layout.select(e1);
        Assert.assertEquals(e1, layout.getSelectedItem());
    }

    /**
     * Test the selection of an item (single item)
     */
    @Test
    @Ignore
    public void testSimpleSearchLayout_Remove() {
        FormOptions options = new FormOptions();
        options.setShowRemoveButton(true);

        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(options);
        layout.build();

        Assert.assertTrue(layout.getRemoveButton().isVisible());

        layout.setSelectedItem(e1);
        layout.checkButtonState(layout.getSelectedItem());
        layout.getRemoveButton().click();

        // check that nothing is selected any more and the item has been removed
        Assert.assertNull(layout.getSelectedItem());
        Assert.assertEquals(2, layout.getGridWrapper().getDataProviderSize());
    }

    /**
     * Test setting a pre-defined search value
     */
    @Test
    public void testSimpleSearchLayout_setSearchValue() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();

        layout.setSearchValue("age", "13", "15");
        layout.search();
        layout.getGridWrapper().forceSearch();

        Assert.assertEquals(1, layout.getGridWrapper().getDataProviderSize());

        // clear all search results
        layout.getSearchForm().getClearButton().click();
        layout.getGridWrapper().forceSearch();
        Assert.assertEquals(3, layout.getGridWrapper().getDataProviderSize());
    }

    @Test
    public void testSimpleSearchLayout_setSearchValueEnum() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
        layout.build();

        layout.setSearchValue("someEnum", TestEnum.A);
        layout.search();
        Assert.assertEquals(0, layout.getGridWrapper().getDataProviderSize());
    }

    /**
     * Test a cascading search
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleSearchLayout_Cascade() {

        Assert.assertEquals(3, testEntityService.findAll().size());

        FormOptions fo = new FormOptions();
        EntityModel<CascadeEntity> model = entityModelFactory.getModel(CascadeEntity.class);
        Assert.assertEquals(1, model.getAttributeModel("testEntity").getCascadeAttributes().size());

        SimpleSearchLayout<Integer, CascadeEntity> layout = new SimpleSearchLayout<Integer, CascadeEntity>(cascadeEntityService,
                entityModelFactory.getModel(CascadeEntity.class), QueryType.ID_BASED, fo, null);
        layout.build();

        QuickAddEntityComboBox<Integer, TestEntity> box1 = (QuickAddEntityComboBox<Integer, TestEntity>) (Object) layout.getSearchForm()
                .getGroups().get("testEntity").getField();
        box1.refresh();

        layout.setSearchValue("testEntity", e1);

        // check that an additional filter for "testEntity" is set
        QuickAddEntityComboBox<Integer, TestEntity2> box2 = (QuickAddEntityComboBox<Integer, TestEntity2>) (Object) layout.getSearchForm()
                .getGroups().get("testEntity2").getField();
        EqualsPredicate<TestEntity2> equal = (EqualsPredicate<TestEntity2>) box2.getAdditionalFilter();
        Assert.assertEquals("testEntity", equal.getProperty());

        // clear filter and verify cascaded filter is removed as well
        layout.setSearchValue("testEntity", null);
        Assert.assertNull(box2.getAdditionalFilter());
    }

    @Test
    @Ignore
    public void testSimpleSearchLayout_DoNotSearchImmediately() {
        SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setSearchImmediately(false));
        layout.build();

        // no search results table yet
        Component label = VaadinUtils.getParentOfClass(layout.getSearchResultsLayout(), Label.class);
        Assert.assertNotNull(label);

        // perform a search, verify the label is removed and replaced by a search
        // results table
        layout.getSearchForm().getSearchButton().click();
        label = VaadinUtils.getParentOfClass(layout.getSearchResultsLayout(), Label.class);
        Assert.assertNull(label);

        ServiceBasedGridWrapper<?, ?> wrapper = VaadinUtils.getParentOfClass(layout.getSearchResultsLayout(),
                ServiceBasedGridWrapper.class);
        Assert.assertNotNull(wrapper);

        // press the clear button and verify the layout is reset
        layout.getSearchForm().getClearButton().click();
        label = VaadinUtils.getParentOfClass(layout.getSearchResultsLayout(), Label.class);
        Assert.assertNotNull(label);
        wrapper = VaadinUtils.getParentOfClass(layout.getSearchResultsLayout(), ServiceBasedGridWrapper.class);
        Assert.assertNull(wrapper);
    }

    private SimpleSearchLayout<Integer, TestEntity> createLayout(FormOptions fo) {
        return new SimpleSearchLayout<>(testEntityService, entityModelFactory.getModel(TestEntity.class), QueryType.ID_BASED, fo,
                new SortOrder<String>("name", SortDirection.ASCENDING));

    }
}
