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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.exception.OCSRuntimeException;

public class ClassUtilsTest {

    @SuppressWarnings("unused")
    private class NestedTestObject {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @SuppressWarnings("unused")
    private class TestObject {

        private byte[] fieldBytes = new byte[] { 1, 2, 3 };

        @Attribute(description = "desc")
        private Integer fieldOne;

        @Size(max = 123)
        private String fieldTwo;

        private NestedTestObject nested;

        public byte[] getFieldBytes() {
            return fieldBytes;
        }

        public Integer getFieldOne() {
            return fieldOne;
        }

        @Attribute(displayName = "Bert")
        public String getFieldThree() {
            return null;
        }

        public String getFieldTwo() {
            return fieldTwo;
        }

        public NestedTestObject getNested() {
            return nested;
        }

        public void setFieldBytes(byte[] fieldBytes) {
            this.fieldBytes = fieldBytes;
        }

        public void setFieldOne(Integer fieldOne) {
            this.fieldOne = fieldOne;
        }

        public void setFieldTwo(String fieldTwo) {
            this.fieldTwo = fieldTwo;
        }

        public void setNested(NestedTestObject nested) {
            this.nested = nested;
        }

    }

    private class TestObject2 extends TestObject {

    }

    private class TestObject3 {
        private Map<Integer, List<TestObject>> indexedObjects;
        private List<TestObject> objects;

        @SuppressWarnings("unused")
        public Map<Integer, List<TestObject>> getIndexedObjects() {
            return indexedObjects;
        }

        @SuppressWarnings("unused")
        public List<TestObject> getObjects() {
            return objects;
        }
    }

    @Test
    public void testClearFieldValue() {
        TestEntity entity = new TestEntity();
        entity.setSomeBytes(new byte[] { 1, 2, 3, 4 });
        entity.setName("Bob");
        entity.setAge(44L);

        ClassUtils.clearFieldValue(entity, "someBytes", byte[].class);
        assertNull(entity.getSomeBytes());

        ClassUtils.clearFieldValue(entity, "name", String.class);
        assertNull(entity.getName());

        TestEntity2 entity2 = new TestEntity2();
        entity2.setTestEntity(entity);

        ClassUtils.clearFieldValue(entity2, "testEntity.age", Long.class);
        assertNull(entity.getAge());
    }

    @Test
    public void testCanSetProperty() {

        TestEntity2 entity2 = new TestEntity2();

        // non-nested
        assertTrue(ClassUtils.canSetProperty(entity2, "name"));
        assertFalse(ClassUtils.canSetProperty(entity2, "phone"));

        // nested property - cannot be set since the reference to testEntity is
        // empty
        assertFalse(ClassUtils.canSetProperty(entity2, "testEntity.name"));

        // the reference is now non-empty and the property can be set
        entity2.setTestEntity(new TestEntity());
        assertTrue(ClassUtils.canSetProperty(entity2, "testEntity.name"));
        // neste property that does not exist
        assertFalse(ClassUtils.canSetProperty(entity2, "testEntity.phone"));
    }

    @Test
    public void testForClass() {
        Class<?> clazz = ClassUtils.forClass("com.ocs.dynamo.domain.TestEntity");
        assertNotNull(clazz);
    }

    @Test
    public void testForClassBogus() {
        Class<?> clazz = ClassUtils.forClass("com.ocs.dynamo.domain.TestEntityZ");
        assertNull(clazz);
    }

    @Test
    public void testForClassNull() {
        Class<?> clazz = ClassUtils.forClass("");
        assertNull(clazz);
    }

    @Test
    public void testGetAnnotationOnField() {
        Field fieldOne = ClassUtils.getField(TestObject.class, "fieldOne");

        // retrieve an annotation from a field
        Attribute attribute = ClassUtils.getAnnotationOnField(fieldOne, Attribute.class);
        assertNotNull(attribute);
        assertEquals("desc", attribute.description());

        // retrieve the annotation from the class and the field name
        attribute = ClassUtils.getAnnotationOnField(TestObject.class, "fieldOne", Attribute.class);
        assertEquals("desc", attribute.description());

        String desc = ClassUtils.getAnnotationAttributeValue(fieldOne, Attribute.class, "description");
        assertEquals("desc", desc);

        // check for null
        Entity entity = ClassUtils.getAnnotationOnField(fieldOne, Entity.class);
        assertNull(entity);
    }

    /**
     * Test that retrieving an annotation from a getter method also works
     */
    @Test
    public void testGetAnnotationOnMethod() {
        Attribute attribute = ClassUtils.getAnnotationOnMethod(TestObject.class, "fieldThree", Attribute.class);
        assertNotNull(attribute);
        assertEquals("Bert", attribute.displayName());
    }

    @Test
    public void testGetBytes() {
        TestEntity entity = new TestEntity();
        entity.setSomeBytes(new byte[] { 1, 2, 3 });

        byte[] bytes = ClassUtils.getBytes(entity, "someBytes");
        assertNotNull(bytes);
        assertEquals(3, bytes.length);

        ClassUtils.setBytes(new byte[] { 2, 3, 4 }, entity, "someBytes");
        bytes = entity.getSomeBytes();
        assertEquals(2, bytes[0]);
        assertEquals(3, bytes[1]);
        assertEquals(4, bytes[2]);

        ClassUtils.setBytes(null, entity, "someBytes");
        assertNull(entity.getSomeBytes());
    }

    /**
     * Basic test case for retrieving a field from a class
     */
    @Test
    public void testGetField() {

        assertNull(ClassUtils.getField(TestObject.class, "bogus"));

        Field fieldOne = ClassUtils.getField(TestObject.class, "fieldOne");
        assertEquals(Integer.class, fieldOne.getType());

        Field fieldTwo = ClassUtils.getField(TestObject.class, "fieldTwo");
        assertEquals(String.class, fieldTwo.getType());
    }

    /**
     * Verify that getting a field also works for a super class
     */
    @Test
    public void testGetFieldRecursive() {
        Field fieldOne = ClassUtils.getField(TestObject2.class, "fieldOne");
        assertEquals(Integer.class, fieldOne.getType());
    }

    @Test
    public void testGetFieldValue() {
        TestEntity entity = new TestEntity();
        entity.setAge(12L);
        assertEquals(12L, ClassUtils.getFieldValue(entity, "age"));
    }

    @Test
    public void testGetFieldValueAsString() {
        TestEntity entity = new TestEntity();
        entity.setAge(12L);

        assertEquals("12", ClassUtils.getFieldValueAsString(entity, "age"));

        entity.setAge(null);
        assertNull(ClassUtils.getFieldValueAsString(entity, "age"));
    }

    @Test
    public void testGetFieldValueDoesntExists() {
        TestEntity entity = new TestEntity();
        entity.setAge(12L);
        assertThrows(OCSRuntimeException.class, () -> ClassUtils.getFieldValue(entity, "age2"));
    }

    @Test
    public void testGetFieldValueRecursive() {
        TestEntity entity = new TestEntity();
        entity.setAge(12L);

        TestEntity2 entity2 = new TestEntity2();
        entity2.setTestEntity(entity);

        assertEquals(12L, ClassUtils.getFieldValue(entity2, "testEntity.age"));
    }

    @Test
    public void testGetGetterMethod() {

        Method method = ClassUtils.getGetterMethod(TestEntity.class, "age");
        assertNotNull(method);

        // non existing method
        Method method3 = ClassUtils.getGetterMethod(TestEntity.class, "age2");
        assertNull(method3);

    }

    @Test
    public void testGetMaxLength() {
        assertEquals(-1, ClassUtils.getMaxLength(TestObject.class, "fieldOne"));
        assertEquals(123, ClassUtils.getMaxLength(TestObject.class, "fieldTwo"));
    }

    /**
     * Basic test case for retrieving a getter method from a class
     */
    @Test
    public void testGetMethod() {

        assertNull(ClassUtils.getGetterMethod(TestObject.class, "bogus"));

        Method method = ClassUtils.getGetterMethod(TestObject.class, "fieldOne");
        assertEquals(Integer.class, method.getReturnType());

        method = ClassUtils.getGetterMethod(TestObject.class, "fieldTwo");
        assertEquals(String.class, method.getReturnType());
    }

    @Test
    public void testGetResolvedType() {
        Class<?> type = ClassUtils.getResolvedType(TestObject3.class, "objects", 0);
        assertNotNull(type);
        assertEquals(TestObject.class, type);

        type = ClassUtils.getResolvedType(TestObject3.class, "indexedObjects", 0);
        assertNotNull(type);
        assertEquals(Integer.class, type);
        type = ClassUtils.getResolvedType(TestObject3.class, "indexedObjects", 1, 0);
        assertNotNull(type);
        assertEquals(TestObject.class, type);
    }

    @Test
    public void testHasMethod() {
        TestEntity entity = new TestEntity();
        assertTrue(ClassUtils.hasMethod(entity, "getAge"));
        assertTrue(ClassUtils.hasMethod(entity, "getSomeBoolean"));

        // check is case sensitive
        assertFalse(ClassUtils.hasMethod(entity, "getAGe"));
        // must provide the full method name
        assertFalse(ClassUtils.hasMethod(entity, "age"));
    }

    @Test
    public void testSetFieldValue() {
        TestEntity entity = new TestEntity();
        ClassUtils.setFieldValue(entity, "age", 12L);

        assertEquals(12L, entity.getAge().longValue());
    }

    @Test
    public void testSetFieldValueDoenstExist() {
        TestEntity entity = new TestEntity();
        assertThrows(OCSRuntimeException.class, () -> ClassUtils.setFieldValue(entity, "age2", 12L));
    }

    @Test
    public void testSetFieldValueRecursive() {
        TestEntity entity = new TestEntity();
        TestEntity2 entity2 = new TestEntity2();
        entity2.setTestEntity(entity);

        ClassUtils.setFieldValue(entity2, "testEntity.age", 12L);

        assertEquals(12L, entity.getAge().longValue());
    }
}
