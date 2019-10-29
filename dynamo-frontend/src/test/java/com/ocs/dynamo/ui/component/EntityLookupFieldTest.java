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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;

public class EntityLookupFieldTest extends BaseMockitoTest {

    private static Routes routes;

    @BeforeClass
    public static void createRoutes() {
        // initialize routes only once, to avoid view auto-detection before every test
        // and to speed up the tests
        routes = new Routes().autoDiscoverViews("com.ocs.dynamo");
    }

    @Mock
    private TestEntityService service;

    @Spy
    private EntityModelFactory factory = new EntityModelFactoryImpl();

    private TestEntity e1;

    private TestEntity e2;

    @Before
    public void setUp() {
        e1 = new TestEntity(1, "Bob", 14L);
        e2 = new TestEntity(2, "Kevin", 15L);

        Mockito.when(service.createNewEntity()).thenReturn(new TestEntity());
        MockUtil.mockServiceSave(service, TestEntity.class);
        MockVaadin.setup(routes);
    }

    @Test
    public void test() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class), null, null,
                false, false, Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)));

        field.initContent();

        Assert.assertEquals("name", field.getSortOrders().get(0).getSorted().toString());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testMultipleSelect() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class), null, null,
                false, true, Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)));
        field.initContent();
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testMultipleSelectWithPreviousValue() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class),
                factory.getModel(TestEntity2.class).getAttributeModel("testEntity"), null, false, true,
                Lists.newArrayList(new SortOrder("name", SortDirection.ASCENDING)));
        field.initContent();
        field.setValue(Lists.newArrayList(e1));
        field.getSelectButton().click();
    }

    @Test
    public void testPageLength() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class), null, null,
                false, false, Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)));
        field.setPageLength(10);
        field.initContent();
        // MockUtil.injectUI(field, ui);

        Assert.assertEquals("name", field.getSortOrders().get(0).getSorted());
        Assert.assertEquals(10, field.getPageLength().intValue());

    }

    /**
     * Test that the clear button has the desired effect
     */
    @Test
    public void testClear() {
        EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service, factory.getModel(TestEntity.class),
                factory.getModel(TestEntity2.class).getAttributeModel("testEntity"), null, false, false,
                Lists.newArrayList(new SortOrder<String>("name", SortDirection.ASCENDING)));
        field.initContent();
        field.setValue(new TestEntity("Kevin", 47L));

        field.getClearButton().click();
        Assert.assertNull(field.getValue());
    }
}
