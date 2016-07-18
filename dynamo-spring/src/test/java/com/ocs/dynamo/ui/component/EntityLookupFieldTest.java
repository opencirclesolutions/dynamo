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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Component;

public class EntityLookupFieldTest extends BaseMockitoTest {

    @Mock
    private TestEntityService service;

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Test
    public void test() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, false, new SortOrder("name",
                        SortDirection.ASCENDING));
        field.initContent();

        Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
        Assert.assertEquals(TestEntity.class, field.getType());

        Component comp = field.iterator().next();
        Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

        try {
            field.getSelectButton().click();
        } catch (com.vaadin.event.ListenerMethod.MethodException ex) {
            // expected since there is no UI
            Assert.assertTrue(ex.getCause() instanceof NullPointerException);
        }

        field.setEnabled(false);
        Assert.assertFalse(field.getSelectButton().isEnabled());
        Assert.assertFalse(field.getClearButton().isEnabled());
    }

    @Test
    public void testPageLength() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
                factory.getModel(TestEntity.class), null, null, false, false, new SortOrder("name",
                        SortDirection.ASCENDING));
        field.setPageLength(10);
        field.initContent();

        Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
        Assert.assertEquals(TestEntity.class, field.getType());

        Component comp = field.iterator().next();
        Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

        try {
            field.getSelectButton().click();
        } catch (com.vaadin.event.ListenerMethod.MethodException ex) {
            // expected since there is no UI
            Assert.assertTrue(ex.getCause() instanceof NullPointerException);
        }

    }
}
