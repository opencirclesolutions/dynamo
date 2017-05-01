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
package com.ocs.dynamo.domain.model.util;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

public class EntityModelUtilTest extends BaseMockitoTest {

    private EntityModelFactory factory = new EntityModelFactoryImpl();

    @Mock
    private MessageService messageService = new MessageServiceImpl();

    @Override
    public void setUp() {
        super.setUp();
        MockUtil.mockMessageService(messageService);
    }

    @Test
    public void testGetMainAttributeValue() {
        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setName("test name");

        String value = EntityModelUtil.getMainAttributeValue(entity, model);
        Assert.assertEquals("test name", value);
    }

    @Test
    public void testGetDisplayPropertyValue() {
        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setName("test name");

        String value = EntityModelUtil.getDisplayPropertyValue(entity, model);
        Assert.assertEquals("test name", value);
    }

    @Test
    public void testGetDisplayPropertyValue2() {
        EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setName("test name");

        TestEntity entity2 = new TestEntity();
        entity2.setName("test name 2");

        String value = EntityModelUtil.getDisplayPropertyValue(Lists.newArrayList(entity, entity2), model, 2,
                messageService);
        Assert.assertEquals("test name, test name 2", value);
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

        String value = EntityModelUtil.getDisplayPropertyValue(Lists.newArrayList(entity, entity2, entity3), model, 2,
                messageService);
        Assert.assertEquals("test name, test name 2, ocs.and.others", value);
    }

    @Test
    public void testCopySimpleAttributes() {
        TestEntity2 source = new TestEntity2();
        source.setId(1);
        source.setName("Name");
        source.setTestEntity(new TestEntity());

        TestEntity2 target = new TestEntity2();

        EntityModelUtil.copySimpleAttributes(source, target, factory.getModel(TestEntity2.class));

        // simple attribute "name" must have been copied
        Assert.assertEquals("Name", target.getName());
        // the ID is never copied
        Assert.assertNull(target.getId());
        // complex attributes are not copied
        Assert.assertNull(target.getTestEntity());
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

        EntityModelUtil.copySimpleAttributes(source, target, factory.getModel(TestEntity.class));

        // simple attribute "name" must have been copied
        Assert.assertEquals("Name", target.getName());
        Assert.assertEquals(12L, target.getAge().longValue());
        Assert.assertEquals(12.0, target.getDiscount().longValue(), 0.001);
        Assert.assertEquals(Boolean.TRUE, target.getSomeBoolean());

        // the ID is never copied
        Assert.assertNull(target.getId());
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

        EntityModelUtil.copySimpleAttributes(source, target, factory.getModel(TestEntity.class), "name");

        // name is listed in the ignore list and is not copied
        Assert.assertNull(target.getName());
    }

    @Test
    public void testCompare() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        // there are no changes
        List<String> changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory,
                messageService);
        Assert.assertEquals(0, changes.size());

        e1.setAge(12L);
        changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        Assert.assertEquals(1, changes.size());
        Assert.assertEquals("ocs.value.changed", changes.get(0));

        e1.setAge(null);
        e2.setName("Kevin");
        changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
        Assert.assertEquals(1, changes.size());
        Assert.assertEquals("ocs.value.changed", changes.get(0));
    }

    @Test
    public void testCompare_Ignore() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        e1.setName("Bob");

        // name has changed
        List<String> changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory,
                messageService);
        Assert.assertEquals(1, changes.size());

        // ignore the name change
        changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService, "name");
        Assert.assertEquals(0, changes.size());
    }

    @Test
    public void testCompare_CollectionRemove() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        e1.addTestEntity2(new TestEntity2());

        List<String> changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory,
                messageService);
        Assert.assertEquals(1, changes.size());
        Assert.assertEquals("ocs.value.removed", changes.get(0));
    }

    @Test
    public void testCompare_CollectionAdd() {
        TestEntity e1 = new TestEntity();
        TestEntity e2 = new TestEntity();

        e2.addTestEntity2(new TestEntity2());

        List<String> changes = EntityModelUtil.compare(e1, e2, factory.getModel(TestEntity.class), factory,
                messageService);
        Assert.assertEquals(1, changes.size());
        Assert.assertEquals("ocs.value.added", changes.get(0));
    }

    @Test
    public void testDefaultCaptionFormat() {
        // Given: some field name.
        String fieldName = "minimumAge";

        // Given: default alternative caption format.
        System.clearProperty(DynamoConstants.SP_DEFAULT_CAPTION_FORMAT);

        // Test: Get caption using configured caption format.
        String caption = EntityModelUtil.getCaptionByPropertyId(fieldName);

        // Assert: Vaadin formatting applied.
        Assert.assertEquals("Minimum Age", caption);
    }

    @Test
    public void testAlternativeCaptionFormat() {
        // Given: some field name.
        String fieldName = "minimumAge";

        // Given: configured alternative caption format.
        String oldValue = System.getProperty(DynamoConstants.SP_DEFAULT_CAPTION_FORMAT);
        System.setProperty(DynamoConstants.SP_DEFAULT_CAPTION_FORMAT, "testAlternativeCaptionFormat");

        // Test: Get caption using configured caption format.
        String caption = EntityModelUtil.getCaptionByPropertyId(fieldName);

        // Assert: The old default caption format was nog configured.
        Assert.assertNull(oldValue);

        // Assert: Alternative formatting applied.
        Assert.assertEquals("Minimum age", caption);
    }
}
