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
import org.junit.Test;

import com.google.common.collect.Sets;
import com.ocs.dynamo.test.BaseIntegrationTest;

public class CollectionTableTest extends BaseIntegrationTest {
	
	@Test
	public void testViewMode() {
		FormOptions fo = new FormOptions();
		CollectionTable<String> table = new CollectionTable<>(true, fo, String.class);

		table.initContent();

		Assert.assertTrue(table.getTable().getContainerPropertyIds().contains("value"));
		Assert.assertEquals(3, table.getTable().getPageLength());
		Assert.assertFalse(table.getTable().isEditable());
	}

	@Test
	public void testEditMode() {
		FormOptions fo = new FormOptions();
		fo.setShowRemoveButton(true);

		CollectionTable<String> table = new CollectionTable<String>(false, fo, String.class);

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
	}
}
