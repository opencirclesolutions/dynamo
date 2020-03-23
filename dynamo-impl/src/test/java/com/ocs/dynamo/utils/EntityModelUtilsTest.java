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
package com.ocs.dynamo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

public class EntityModelUtilsTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private MessageService messageService = new MessageServiceImpl();

    @BeforeEach
    public void setUp() {
        MockUtil.mockMessageService(messageService);
    }

    @Test
    public void testGetDisplayPropertyValue() {
        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setName("test name");

        String value = EntityModelUtils.getDisplayPropertyValue(entity, model);
        assertEquals("test name", value);
    }

    @Test
    public void testGetDisplayPropertyValue2() {
        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setName("test name");

        TestEntity entity2 = new TestEntity();
        entity2.setName("test name 2");

        String value = EntityModelUtils.getDisplayPropertyValue(Lists.newArrayList(entity, entity2), model, 2, messageService,
                Locale.ENGLISH);
        assertEquals("test name, test name 2", value);
    }

    /**
     * Test truncation of the description
     */
    @Test
    public void testGetDisplayPropertyValue3() {
        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setName("test name");

        TestEntity entity2 = new TestEntity();
        entity2.setName("test name 2");

        TestEntity entity3 = new TestEntity();
        entity3.setName("test name 3");

        String value = EntityModelUtils.getDisplayPropertyValue(Lists.newArrayList(entity, entity2, entity3), model, 2, messageService,
                Locale.ENGLISH);
        assertEquals("test name, test name 2, ocs.and.others", value);
    }

    @Test
    public void testCopySimpleAttributes() {
        TestEntity2 source = new TestEntity2();
        source.setId(1);
        source.setName("Name");
        source.setTestEntity(new TestEntity());

        TestEntity2 target = new TestEntity2();

        EntityModelUtils.copySimpleAttributes(source, target, factory.getModel(TestEntity2.class));

        // simple attribute "name" must have been copied
        assertEquals("Name", target.getName());
        // the ID is never copied
        assertNull(target.getId());
        // complex attributes are not copied
        assertNull(target.getTestEntity());
    }

    @Test
    public void testCopySimpleAttributes2() {
        TestEntity source = new TestEntity();
        source.setAge(12L);
        source.setName("Name");
        source.setDiscount(BigDecimal.valueOf(12.0));
        source.setSomeBoolean(Boolean.TRUE);
        source.setId(24);

        TestEntity target = new TestEntity();

        EntityModelUtils.copySimpleAttributes(source, target, factory.getModel(TestEntity.class));

        // simple attribute "name" must have been copied
        assertEquals("Name", target.getName());
        assertEquals(12L, target.getAge().longValue());
        assertEquals(12.0, target.getDiscount().longValue(), 0.001);
        assertEquals(Boolean.TRUE, target.getSomeBoolean());

        // the ID is never copied
        assertNull(target.getId());
    }

    /**
     * Test that some attributes can be ignored
     */
    @Test
    public void testCopySimpleAttributes_Ignore() {
        TestEntity source = new TestEntity();
        source.setAge(12L);
        source.setName("Name");
        source.setDiscount(BigDecimal.valueOf(12.0));
        source.setSomeBoolean(Boolean.TRUE);
        source.setId(24);

        TestEntity target = new TestEntity();

        EntityModelUtils.copySimpleAttributes(source, target, factory.getModel(TestEntity.class), "name");

        // name is listed in the ignore list and is not copied
        assertNull(target.getName());
    }

}
