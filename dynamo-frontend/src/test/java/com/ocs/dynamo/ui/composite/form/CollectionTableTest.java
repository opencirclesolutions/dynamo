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
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;

public class CollectionTableTest extends BaseIntegrationTest {

    @Autowired
    private EntityModelFactory emf;

    @Test
    public void testViewMode() {
        EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

        FormOptions fo = new FormOptions();
        CollectionTable<String> table = new CollectionTable<>(em.getAttributeModel("tags"), true, fo);

        table.initContent();

        Assert.assertTrue(table.getTable().getContainerPropertyIds().contains("value"));
        Assert.assertEquals(3, table.getTable().getPageLength());
        Assert.assertFalse(table.getTable().isEditable());
    }

    /**
     * Test the creation of a table for integers
     */
    @Test
    public void testTableOfIntegers() {
        EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

        FormOptions fo = new FormOptions();
        CollectionTable<Integer> table = new CollectionTable<>(em.getAttributeModel("intTags"), false, fo);
        table.initContent();

        table.getAddButton().click();

        table.setValue(Sets.newHashSet(4, 5));
        Assert.assertEquals(2, table.getTable().getItemIds().size());
    }

    @Test
    public void testTableOfLongs() {
        EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

        FormOptions fo = new FormOptions();
        CollectionTable<Long> table = new CollectionTable<>(em.getAttributeModel("longTags"), false, fo);
        table.initContent();

        table.getAddButton().click();

        table.setValue(Sets.newHashSet(4L, 5L));
        Assert.assertEquals(2, table.getTable().getItemIds().size());
    }

    @Test
    public void testEditMode() {
        EntityModel<TestEntity> em = emf.getModel(TestEntity.class);

        FormOptions fo = new FormOptions().setShowRemoveButton(true);
        CollectionTable<String> table = new CollectionTable<>(em.getAttributeModel("tags"), false, fo);

        table.initContent();

        Assert.assertNotNull(table.getTable().getColumnGenerator("Remove"));

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
