package com.ocs.dynamo.ui.composite.form;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.ocs.dynamo.test.BaseIntegrationTest;

public class CollectionTableTest extends BaseIntegrationTest {

	@Test
	public void testViewMode() {
		FormOptions fo = new FormOptions();
		CollectionTable table = new CollectionTable(true, fo);

		table.initContent();

		Assert.assertTrue(table.getTable().getContainerPropertyIds().contains("value"));
		Assert.assertEquals(3, table.getTable().getPageLength());
		Assert.assertFalse(table.getTable().isEditable());
	}

	@Test
	public void testEditMode() {
		FormOptions fo = new FormOptions();
		fo.setShowRemoveButton(true);

		CollectionTable table = new CollectionTable(false, fo);

		table.initContent();

		Assert.assertTrue(table.getTable().getContainerPropertyIds().contains("value"));
		Assert.assertEquals(3, table.getTable().getPageLength());
		Assert.assertTrue(table.getTable().isEditable());

		// set the values and check that they properly end up in the table
		Set<String> values = Sets.newHashSet("a", "b", "c");
		table.setInternalValue(values);

		Assert.assertEquals(3, table.getTable().getItemIds().size());

		// click the add button and verify that an extra item is added
		table.getAddButton().click();
		Assert.assertEquals(4, table.getTable().getItemIds().size());

		// select the first item
		table.getTable().setValue(1);
		Assert.assertEquals(1, table.getSelectedItem());

		// remove an item
		table.getRemoveButton().click();
	}
}
