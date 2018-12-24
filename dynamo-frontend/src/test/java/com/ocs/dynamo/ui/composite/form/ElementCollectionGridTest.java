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
package com.ocs.dynamo.ui.composite.form;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.vaadin.data.provider.ListDataProvider;

public class ElementCollectionGridTest extends BaseIntegrationTest {

	@Autowired
	private TestEntityService testEntityService;

	@Autowired
	private EntityModelFactory emf;

	@Before
	public void setUp() {
		TestEntity e1 = new TestEntity("Bob", 12L);
		testEntityService.save(e1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTableOfStrings() {
		EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

		FormOptions fo = new FormOptions();
		ElementCollectionGrid<Integer, TestEntity, String> grid = new ElementCollectionGrid<>(
				em.getAttributeModel("tags"), fo);
		grid.initContent();

		grid.setValue(Lists.newArrayList("tag1", "tag2"));

		grid.getAddButton().click();

		ListDataProvider<ValueHolder<String>> provider = (ListDataProvider<ValueHolder<String>>) grid.getGrid()
				.getDataProvider();
		Assert.assertEquals(3, provider.getItems().size());
	}

	/**
	 * Test the creation of a table for integers
	 */
	@Test
	public void testTableOfIntegers() {
		ElementCollectionGrid<Integer, TestEntity, Integer> grid = null;

		EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

		FormOptions fo = new FormOptions();

		grid = new ElementCollectionGrid<>(em.getAttributeModel("intTags"), fo);
		grid.initContent();
		grid.setValue(Sets.newHashSet(4, 5));

		grid.getGrid().select(new ValueHolder<Integer>(4));

		Assert.assertEquals(1, grid.getGrid().getColumns().size());
	}

	@Test
	public void testTableOfLongs() {
		EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

		FormOptions fo = new FormOptions();
		ElementCollectionGrid<Integer, TestEntity, Long> grid = new ElementCollectionGrid<>(
				em.getAttributeModel("longTags"), fo);

		grid.setValue(Sets.newHashSet(4L, 5L));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEditMode() {
		EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

		FormOptions fo = new FormOptions().setShowRemoveButton(true);
		ElementCollectionGrid<Integer, TestEntity, String> grid = new ElementCollectionGrid<>(
				em.getAttributeModel("tags"), fo);

		grid.initContent();

		// set the values and check that they properly end up in the table
		Set<String> values = Sets.newHashSet("a", "b", "c");
		grid.setValue(values);

		ListDataProvider<ValueHolder<String>> list = (ListDataProvider<ValueHolder<String>>) grid.getGrid()
				.getDataProvider();
		Assert.assertEquals(3, list.getItems().size());

		// click the add button and verify that an extra item is added
		grid.getAddButton().click();
		list = (ListDataProvider<ValueHolder<String>>) grid.getGrid().getDataProvider();
		Assert.assertEquals(4, list.getItems().size());

	}
}
