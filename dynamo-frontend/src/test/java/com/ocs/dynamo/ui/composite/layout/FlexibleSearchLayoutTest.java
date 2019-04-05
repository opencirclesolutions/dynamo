package com.ocs.dynamo.ui.composite.layout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;

public class FlexibleSearchLayoutTest extends FrontendIntegrationTest {

	@Inject
	private EntityModelFactory entityModelFactory;

	@Inject
	private TestEntityService testEntityService;

	private TestEntity e1;

	@Before
	public void setup() {
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

		Assert.assertTrue(layout.getAddButton().isVisible());
		Assert.assertTrue(layout.getEditButton().isVisible());
		Assert.assertFalse(layout.getRemoveButton().isVisible());

		Assert.assertEquals("name", layout.getSortOrders().get(0).getSorted());
		Assert.assertEquals(SortDirection.ASCENDING, layout.getSortOrders().get(0).getDirection());

		TestEntity entity = layout.createEntity();
		Assert.assertNotNull(entity);

		Assert.assertEquals(3, layout.getGridWrapper().getDataProviderSize());

		layout.select(e1);
	}

	@Test
	public void testFlexibleSearchLayout_AddButton() {
		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
		layout.build();

		// click the add button and verify that a new item is added
		layout.getAddButton().click();
		Assert.assertNotNull(layout.getSelectedItem());
	}

	@Test
	public void testFlexibleSearchLayout_EditButton() {
		FormOptions options = new FormOptions().setEditAllowed(true);

		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(options);
		layout.build();

		Assert.assertTrue(layout.getEditButton().isVisible());

		// click the add button and verify that a new item is added
		layout.setSelectedItem(e1);
		layout.getEditButton().click();
		Assert.assertEquals(e1, layout.getSelectedItem());
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

		Assert.assertEquals("name", layout.getSortOrders().get(0).getSorted());
		Assert.assertEquals(SortDirection.ASCENDING, layout.getSortOrders().get(0).getDirection());

		Assert.assertEquals(1, layout.getGridWrapper().getDataProviderSize());
	}

	/**
	 * Test the selection of an item (single item)
	 */
	@Test
	public void testFlexibleSearchLayout_Select() {
		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
		layout.build();

		layout.select(e1);
		Assert.assertEquals(e1, layout.getSelectedItem());
	}

	@Test
	public void testFlexibleSearchLayout_SelectCollection() {
		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
		layout.build();
		layout.select(e1);
		Assert.assertEquals(e1, layout.getSelectedItem());
	}

	/**
	 * Test the selection of an item (single item)
	 */
	@Test
	public void testFlexibleSearchLayout_Remove() {
		FormOptions options = new FormOptions();
		options.setShowRemoveButton(true);

		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(options);
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
	public void testFlexibleSearchLayout_setSearchValue() {
		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout(new FormOptions());
		layout.build();

		layout.setSearchValue("age", "13", "15");
		layout.search();

		// verify that this creates a filter
		Assert.assertTrue(layout.getSearchForm()
				.hasFilter(entityModelFactory.getModel(TestEntity.class).getAttributeModel("age")));
		Assert.assertEquals(1, layout.getGridWrapper().getDataProviderSize());
	}

	/**
	 * Test the functionality for making search fields required
	 */
	@Test
	public void testFlexibleSearchLayout_Mandatory() {
		FlexibleSearchLayout<Integer, TestEntity> layout = createLayout("TestEntitySearchRequired", new FormOptions());
		layout.build();

		Assert.assertFalse(layout.getSearchForm().isSearchAllowed());
		layout.setSearchValue("name", "abc");
		Assert.assertTrue(layout.getSearchForm().isSearchAllowed());
	}

	private FlexibleSearchLayout<Integer, TestEntity> createLayout(FormOptions fo) {
		return new FlexibleSearchLayout<>(testEntityService, entityModelFactory.getModel(TestEntity.class),
				QueryType.ID_BASED, fo, new SortOrder<String>("name", SortDirection.ASCENDING));

	}

	private FlexibleSearchLayout<Integer, TestEntity> createLayout(String reference, FormOptions fo) {
		return new FlexibleSearchLayout<>(testEntityService, entityModelFactory.getModel(reference, TestEntity.class),
				QueryType.ID_BASED, fo, new SortOrder<String>("name", SortDirection.ASCENDING));

	}
}
