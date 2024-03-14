package com.ocs.dynamo.ui.composite.layout;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleSearchLayoutTest extends FrontendIntegrationTest {

	@Autowired
	private EntityModelFactory entityModelFactory;

	@Autowired
	private TestEntityService testEntityService;

	private TestEntity e1;

	private TestEntity e2;

	private TestEntity e3;

	private boolean detailsTabCreated = false;

	@BeforeEach
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Kevin", 12L);
		e2 = testEntityService.save(e2);

		e3 = new TestEntity("Stewart", 14L);
		e3 = testEntityService.save(e3);

		MockVaadin.setup();
	}

	@Test
	public void testSimpleSearchLayout() {
		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setShowIterationButtons(true));
		layout.build();

		assertTrue(layout.getAddButton().isVisible());
		assertTrue(layout.getEditButton().isVisible());
		assertFalse(layout.getRemoveButton().isVisible());

		assertEquals("name", layout.getSortOrders().get(0).getSorted());

		TestEntity entity = layout.getCreateEntity().get();
		assertNotNull(entity);

		layout.getGridWrapper().forceSearch();
		assertEquals(3, layout.getGridWrapper().getDataProviderSize());

		layout.select(e2);
		layout.detailsMode(e2);

		// click the next button
		assertEquals(2, layout.getEditForm().getNextButtons().size());
		layout.getEditForm().getNextButtons().iterator().next().click();
		assertEquals(e3, layout.getEditForm().getEntity());
		assertTrue(layout.hasPrevEntity());
		assertFalse(layout.hasNextEntity());

		// click the previous button
		assertEquals(2, layout.getEditForm().getPreviousButtons().size());
		layout.getEditForm().getPreviousButtons().iterator().next().click();
		assertEquals(e2, layout.getEditForm().getEntity());
	}

	/**
	 * Check that a complex details tab is created when requested
	 */
	@Test
	public void testSimpleSearchLayout_ComplexDetailMode() {
		detailsTabCreated = false;
		SimpleSearchLayout<Integer, TestEntity> layout = new SimpleSearchLayout<Integer, TestEntity>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), QueryType.ID_BASED,
				new FormOptions().setComplexDetailsMode(true), new SortOrder<String>("id", SortDirection.ASCENDING));
		layout.setDetailsModeTabCaptions(new String[] { "title1" });
		layout.addDetailTabCreator(0, (fo, isNew) -> {
			detailsTabCreated = true;
			return new VerticalLayout();
		});
		layout.build();

		// click add button, verify that we just get a simple edit screen and not
		// the tab layout
		layout.getAddButton().click();
		assertTrue(detailsTabCreated);

		// select the item and verify that the proper details tab is created
		layout.setSelectedItem(e1);
		layout.getEditButton().click();
		assertTrue(detailsTabCreated);

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
		assertNotNull(layout.getSelectedItem());

		assertFalse(layout.isInSearchMode());
		assertFalse(layout.getEditForm().isViewMode());

		layout.getEditForm().getSaveButtons().get(0).click();
		assertFalse(layout.isInSearchMode());
	}

	@Test
	public void testSimpleSearchLayout_AddButton2() {
		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setOpenInViewMode(false));
		layout.build();

		// click the add button and verify that a new item is added
		layout.getAddButton().click();
		assertNotNull(layout.getSelectedItem());

		assertFalse(layout.isInSearchMode());
		assertFalse(layout.getEditForm().isViewMode());

		layout.getEditForm().getSaveButtons().get(0).click();
		assertFalse(layout.isInSearchMode());
	}

	@Test
	public void testSimpleSearchLayout_EditButton() {
		FormOptions options = new FormOptions();
		options.setShowEditButton(true).setOpenInViewMode(false);

		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(options);
		layout.build();

		assertTrue(layout.getEditButton().isVisible());

		// select an item and edit
		layout.setSelectedItem(e1);
		layout.getEditButton().click();
		assertEquals(e1, layout.getSelectedItem());
		assertFalse(layout.getEditForm().isViewMode());

		// save changes and check that we go back to search mode
		layout.getEditForm().getSaveButtons().get(0).click();
		assertTrue(layout.isInSearchMode());
	}

	@Test
	public void testSimpleSearchLayout_EditButton2() {
		FormOptions options = new FormOptions();
		options.setShowEditButton(true).setOpenInViewMode(true);

		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(options);
		layout.build();

		assertTrue(layout.getEditButton().isVisible());

		// select an item and edit
		layout.setSelectedItem(e1);
		layout.getEditButton().click();
		assertEquals(e1, layout.getSelectedItem());
		assertTrue(layout.getEditForm().isViewMode());
		layout.getEditForm().getEditButtons().get(0).click();
		assertFalse(layout.getEditForm().isViewMode());

		// save changes and check that are back on the detail screen is view mode
		layout.getEditForm().getSaveButtons().get(0).click();
		assertTrue(layout.getEditForm().isViewMode());
		assertFalse(layout.isInSearchMode());
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

		assertEquals("name", layout.getSortOrders().get(0).getSorted());
		assertEquals(SortDirection.ASCENDING, layout.getSortOrders().get(0).getDirection());
		assertEquals(1, layout.getGridWrapper().getDataProviderSize());
	}

	/**
	 * Test the selection of an item (single item)
	 */
	@Test
	public void testSimpleSearchLayout_Select() {
		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
		layout.build();
		layout.select(e1);
		assertEquals(e1, layout.getSelectedItem());
	}

	/**
	 * Test the selection of an item (single item)
	 */
	@Test
	public void testSimpleSearchLayout_Remove() {
		FormOptions options = new FormOptions();
		options.setShowRemoveButton(true);

		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(options);
		layout.build();

		assertTrue(layout.getRemoveButton().isVisible());

		layout.setSelectedItem(e1);
		layout.checkComponentState(layout.getSelectedItem());
		layout.getRemoveButton().click();
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

		assertEquals(1, layout.getGridWrapper().getDataProviderSize());

		// clear all search results
		layout.getSearchForm().getClearButton().click();
		layout.getGridWrapper().forceSearch();
		assertEquals(3, layout.getGridWrapper().getDataProviderSize());
	}

	@Test
	public void testSimpleSearchLayout_setSearchValueEnum() {
		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
		layout.build();

		layout.setSearchValue("someEnum", TestEnum.A);
		layout.search();
		assertEquals(0, layout.getGridWrapper().getDataProviderSize());
	}

	@Test
	public void testSimpleSearchLayout_DoNotSearchImmediately() {
		SimpleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions().setSearchImmediately(false));
		layout.build();

		// no search results table yet
		Component label = VaadinUtils.getFirstChildOfClass(layout.getSearchResultsLayout(), Text.class);
		assertNotNull(label);

		// perform a search, verify the label is removed and replaced by a search
		// results table
		layout.getSearchForm().getSearchButton().click();
		label = VaadinUtils.getFirstChildOfClass(layout.getSearchResultsLayout(), Text.class);
		assertNull(label);

		ServiceBasedGridWrapper<?, ?> wrapper = VaadinUtils.getFirstChildOfClass(layout.getSearchResultsLayout(),
				ServiceBasedGridWrapper.class);
		assertNotNull(wrapper);

		// press the clear button and verify the layout is reset
		layout.getSearchForm().getClearButton().click();
		label = VaadinUtils.getFirstChildOfClass(layout.getSearchResultsLayout(), Text.class);
		assertNotNull(label);
		wrapper = VaadinUtils.getFirstChildOfClass(layout.getSearchResultsLayout(), ServiceBasedGridWrapper.class);
		assertNull(wrapper);
	}

	private SimpleSearchLayout<Integer, TestEntity> createLayout(FormOptions fo) {
		return new SimpleSearchLayout<>(testEntityService, entityModelFactory.getModel(TestEntity.class),
				QueryType.ID_BASED, fo, new SortOrder<String>("name", SortDirection.ASCENDING));

	}
}
