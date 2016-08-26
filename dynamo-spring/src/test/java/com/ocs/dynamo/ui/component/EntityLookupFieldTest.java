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
package com.ocs.dynamo.ui.component;

import java.util.Collection;
import java.util.List;

import junitx.util.PrivateAccessor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.dialog.SimpleModalDialog;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

public class EntityLookupFieldTest extends BaseMockitoTest {

    @Mock
    private UI ui;

    @Mock
    private TestEntityService service;

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    private TestEntity e1;

    private TestEntity e2;

    @Override
    public void setUp() throws Exception {
        TestEntity e1 = new TestEntity("Bob", 14L);
        e1.setId(1);

        TestEntity e2 = new TestEntity("Kevin", 15L);
        e2.setId(2);

        Mockito.when(service.getEntityClass()).thenReturn(TestEntity.class);

        Mockito.when(
                service.findIds(Matchers.any(Filter.class),
                        (com.ocs.dynamo.dao.SortOrder[]) Matchers.anyVararg())).thenReturn(
                Lists.newArrayList(1, 2));

        Mockito.when(
                service.fetchByIds(Matchers.any(List.class), Matchers.any(SortOrders.class),
                        (FetchJoinInformation[]) Matchers.anyVararg())).thenReturn(
                Lists.newArrayList(e1, e2));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void test() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, false, new SortOrder("name",
                        SortDirection.ASCENDING));
        injectUI(field);

        field.initContent();

        Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
        Assert.assertEquals(Object.class, field.getType());

        Component comp = field.iterator().next();
        Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

        ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor
                .forClass(ModelBasedSearchDialog.class);

        field.getSelectButton().click();
        Mockito.verify(ui).addWindow(captor.capture());

        captor.getValue().getOkButton().click();

        field.setEnabled(false);
        Assert.assertFalse(field.getSelectButton().isEnabled());
        Assert.assertFalse(field.getClearButton().isEnabled());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testMultipleSelect() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, true, new SortOrder("name",
                        SortDirection.ASCENDING));
        injectUI(field);
        field.initContent();

        ArgumentCaptor<ModelBasedSearchDialog> captor = ArgumentCaptor
                .forClass(ModelBasedSearchDialog.class);

        field.getSelectButton().click();
        Mockito.verify(ui).addWindow(captor.capture());

        ModelBasedSearchDialog<Integer, TestEntity> dialog = (ModelBasedSearchDialog<Integer, TestEntity>) captor
                .getValue();
        dialog.getOkButton().click();
    }

    @Test
    public void testPageLength() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, false, new SortOrder("name",
                        SortDirection.ASCENDING));
        field.setPageLength(10);
        field.initContent();
        injectUI(field);

        Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
        Assert.assertEquals(10, field.getPageLength().intValue());

    }

    /**
     * Test that the clear button has the desired effect
     */
    @Test
    public void testClear() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, false, new SortOrder("name",
                        SortDirection.ASCENDING));
        field.initContent();
        field.setValue(new TestEntity("Kevin", 47L));

        field.getClearButton().click();
        Assert.assertNull(field.getValue());
    }

    @Test
    public void testAdd() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, false, new SortOrder("name",
                        SortDirection.ASCENDING));
        field.initContent();
        injectUI(field);

        // check that a model dialog is shown
        ArgumentCaptor<SimpleModalDialog> captor = ArgumentCaptor.forClass(SimpleModalDialog.class);

        field.getSelectButton().click();
        Mockito.verify(ui).addWindow(captor.capture());
    }

    /**
     * Injects the current UI
     * 
     * @param field
     */
    private void injectUI(EntityLookupField<Integer, TestEntity> field) {
        try {
            PrivateAccessor.setField(field, "ui", ui);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
