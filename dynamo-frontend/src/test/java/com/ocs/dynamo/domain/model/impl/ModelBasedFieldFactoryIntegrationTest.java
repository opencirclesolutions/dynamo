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
package com.ocs.dynamo.domain.model.impl;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.FancyListSelect;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSelect;
import com.ocs.dynamo.ui.component.TokenFieldSelect;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Field;

public class ModelBasedFieldFactoryIntegrationTest extends BaseIntegrationTest {

    @Inject
    private EntityModelFactory entityModelFactory;

    @Inject
    private MessageService messageService;

    /**
     * Test the creation of
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateLookupField() {

        EntityModel<TestEntity2> model = entityModelFactory.getModel("TestEntity2Lookup", TestEntity2.class);
        AttributeModel am = model.getAttributeModel("testEntity");

        ModelBasedFieldFactory<TestEntity2> factory = new ModelBasedFieldFactory<>(model, messageService, false, false);

        Field<?> field = factory.createField(am.getName());
        Assert.assertTrue(field instanceof EntityLookupField);

        EntityLookupField<Integer, TestEntity> f = (EntityLookupField<Integer, TestEntity>) field;
        Assert.assertEquals(new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING),
                f.getSortOrders().get(0));

    }

    /**
     * Test the creation of a ListSelectComponent
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateListSelect() {

        EntityModel<TestEntity2> model = entityModelFactory.getModel("TestEntity2ListSelect", TestEntity2.class);
        AttributeModel am = model.getAttributeModel("testEntity");

        ModelBasedFieldFactory<TestEntity2> factory = new ModelBasedFieldFactory<>(model, messageService, false, false);

        Field<?> field = factory.createField(am.getName());
        Assert.assertTrue(field instanceof QuickAddListSelect);

        QuickAddListSelect<Integer, TestEntity> f = (QuickAddListSelect<Integer, TestEntity>) field;
        Assert.assertEquals(new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING),
                f.getListSelect().getSortOrders()[0]);
        Assert.assertEquals(com.ocs.dynamo.ui.component.EntityListSelect.SelectMode.FILTERED,
                f.getListSelect().getSelectMode());
        Assert.assertEquals(3, f.getListSelect().getRows());
    }

    /**
     * Test that a token select is created
     */
    @Test
    public void testCreateTokenSelect() {

        EntityModel<TestEntity2> model = entityModelFactory.getModel("TestEntity2ListSelectFancy", TestEntity2.class);
        AttributeModel am = model.getAttributeModel("testEntity");
        Assert.assertTrue(am.isMultipleSearch());

        ModelBasedFieldFactory<TestEntity2> factory = new ModelBasedFieldFactory<>(model, messageService, false, true);

        Field<?> field = factory.createField(am.getName());
        Assert.assertTrue(field instanceof TokenFieldSelect);
    }

    /**
     * Test that a token field is created when specified
     */
    @Test
    public void testCreateListSelectFancyForMultiSearch2() {

        EntityModel<TestEntity2> model = entityModelFactory.getModel("TestEntity3ListSelectFancy", TestEntity2.class);
        AttributeModel am = model.getAttributeModel("testEntity");
        Assert.assertTrue(am.isMultipleSearch());

        ModelBasedFieldFactory<TestEntity2> factory = new ModelBasedFieldFactory<>(model, messageService, false, true);

        Field<?> field = factory.createField(am.getName());
        Assert.assertTrue(field instanceof TokenFieldSelect);
    }

    /**
     * Test that a fancy list select is created for searching for a DETAIL relation
     */
    @Test
    public void testCreateListSelectFancy() {

        EntityModel<TestEntity> model = entityModelFactory.getModel("TestEntityFancy", TestEntity.class);
        AttributeModel am = model.getAttributeModel("testEntities");
        Assert.assertTrue(AttributeSelectMode.FANCY_LIST.equals(am.getSelectMode()));

        ModelBasedFieldFactory<TestEntity> factory = new ModelBasedFieldFactory<>(model, messageService, false, true);

        Field<?> field = factory.createField(am.getName());
        Assert.assertTrue(field instanceof FancyListSelect);
    }

    /**
     * Test the creation of a combo box
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateComboBox() {
        ModelBasedFieldFactory<TestEntity2> fieldFactory = ModelBasedFieldFactory
                .getInstance(entityModelFactory.getModel(TestEntity2.class), messageService);

        EntityModel<TestEntity2> model = entityModelFactory.getModel(TestEntity2.class);
        AttributeModel am = model.getAttributeModel("testEntity");

        Field<?> field = fieldFactory.constructComboBox(am.getNestedEntityModel(), am, null, false);
        Assert.assertTrue(field instanceof QuickAddEntityComboBox);

        QuickAddEntityComboBox<Integer, TestEntity> dc = (QuickAddEntityComboBox<Integer, TestEntity>) field;
        Assert.assertEquals(SelectMode.FILTERED, dc.getComboBox().getSelectMode());

        SortOrder[] sortOrders = dc.getComboBox().getSortOrder();
        Assert.assertEquals(2, sortOrders.length);

        Assert.assertEquals("name", sortOrders[0].getPropertyId());
        Assert.assertEquals(SortDirection.ASCENDING, sortOrders[0].getDirection());

        Assert.assertEquals("age", sortOrders[1].getPropertyId());
        Assert.assertEquals(SortDirection.ASCENDING, sortOrders[1].getDirection());
    }
}
