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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;

public class EntityLookupFieldTest extends BaseMockitoTest {

    @Mock
    private TestEntityService service;

    @Spy
    private EntityModelFactory factory = new EntityModelFactoryImpl();

    private TestEntity e1;

    @BeforeEach
    public void setUp() {
        e1 = new TestEntity(1, "Bob", 14L);

        when(service.createNewEntity()).thenReturn(new TestEntity());
        MockUtil.mockServiceSave(service, TestEntity.class);
        MockVaadin.setup();
    }

    @Test
    public void test() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class), null, null,
                false, false, false, Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)));
        field.initContent();
        assertEquals("name", field.getSortOrders().get(0).getSorted().toString());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testMultipleSelectWithPreviousValue() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class),
                factory.getModel(TestEntity2.class).getAttributeModel("testEntity"), null, false, true, false,
                Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)));
        field.initContent();
        field.setValue(Lists.newArrayList(e1));
        field.getSelectButton().click();
        Collection<TestEntity> col = (Collection<TestEntity>) field.getValue();
        assertTrue(col.contains(e1));
    }

    /**
     * Test that the clear button has the desired effect
     */
    @Test
    public void testClear() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class),
                factory.getModel(TestEntity2.class).getAttributeModel("testEntity"), null, false, false, false,
                Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)));
        field.initContent();
        field.setValue(new TestEntity("Kevin", 47L));

        field.getClearButton().click();
        assertNull(field.getValue());
    }
}
