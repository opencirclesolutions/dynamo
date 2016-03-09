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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.data.util.filter.Compare;

import junitx.util.PrivateAccessor;

public class EntityComboBoxTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private TestEntityService service;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PrivateAccessor.setField(factory, "defaultPrecision", 2);
    }

    @Test
    public void test() {

    }

    @Test
    public void testAll() {
        AttributeModel am = factory.getModel(TestEntity.class).getAttributeModel("name");

        EntityComboBox<Integer, TestEntity> select = new EntityComboBox<>(
                factory.getModel(TestEntity.class), am, service);
        Assert.assertEquals(EntityComboBox.SelectMode.ALL, select.getSelectMode());
        Assert.assertEquals(am, select.getAttributeModel());

        Mockito.verify(service).findAll((SortOrder[]) null);
    }

    @Test
    public void testFixed() {
        EntityComboBox<Integer, TestEntity> select = new EntityComboBox<>(
                factory.getModel(TestEntity.class), null, Lists.newArrayList(new TestEntity()));
        Assert.assertEquals(EntityComboBox.SelectMode.FIXED, select.getSelectMode());

        Mockito.verifyZeroInteractions(service);
    }

    @Test
    public void testFilter() {
        EntityComboBox<Integer, TestEntity> select = new EntityComboBox<>(
                factory.getModel(TestEntity.class), null, service,
                new Compare.Equal("name", "Bob"));
        Assert.assertEquals(EntityComboBox.SelectMode.FILTERED, select.getSelectMode());

        Mockito.verify(service).find(Matchers.any(com.ocs.dynamo.filter.Filter.class),
                Matchers.any(com.ocs.dynamo.dao.SortOrder[].class));
    }
}
