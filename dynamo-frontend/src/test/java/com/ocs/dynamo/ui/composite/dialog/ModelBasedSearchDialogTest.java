package com.ocs.dynamo.ui.composite.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;

public class ModelBasedSearchDialogTest extends BaseIntegrationTest {

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
	public void testCreateSingleSelect() {
		ModelBasedSearchDialog<Integer, TestEntity> dialog = new ModelBasedSearchDialog<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), new ArrayList<>(), null, false, true);
		dialog.setPageLength(4);
		dialog.build();

		Assert.assertEquals(4, dialog.getSearchLayout().getPageLength());

		// check that there are two items
		Assert.assertEquals(2, dialog.getSearchLayout().getTableWrapper().getTable().size());
		dialog.select(e1.getId());
	}

	@Test
	public void testCreateSingleSelectWithFilter() {
		List<Filter> filters = new ArrayList<>();
		filters.add(new Compare.Equal("name", "Bob"));

		ModelBasedSearchDialog<Integer, TestEntity> dialog = new ModelBasedSearchDialog<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), filters, null, false, true);
		dialog.build();

		// only one item matches the filter
		Assert.assertEquals(1, dialog.getSearchLayout().getTableWrapper().getTable().size());

	}

	@Test
	public void testCreateMultiSelect() {
		ModelBasedSearchDialog<Integer, TestEntity> dialog = new ModelBasedSearchDialog<>(testEntityService,
				entityModelFactory.getModel(TestEntity.class), new ArrayList<>(), null, true, true);
		dialog.build();

		// check that there are two items
		Assert.assertEquals(2, dialog.getSearchLayout().getTableWrapper().getTable().size());
		// check that you can select both
		dialog.select(Lists.newArrayList(e1.getId(), e2.getId()));
	}

}
