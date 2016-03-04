package com.ocs.dynamo.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertNull(entity.getSomeBytes());

		ClassUtils.clearFieldValue(entity, "name", String.class);
		Assert.assertNull(entity.getName());

		TestEntity2 entity2 = new TestEntity2();
		entity2.setTestEntity(entity);

		ClassUtils.clearFieldValue(entity2, "testEntity.age", Long.class);
		Assert.assertNull(entity.getAge());
	}

	@Test
	public void testCanSetProperty() {

		TestEntity2 entity2 = new TestEntity2();

		// non-nested
		Assert.assertTrue(ClassUtils.canSetProperty(entity2, "name"));
		Assert.assertFalse(ClassUtils.canSetProperty(entity2, "phone"));

		// nested property - cannot be set since the reference to testEntity is
		// empty
		Assert.assertFalse(ClassUtils.canSetProperty(entity2, "testEntity.name"));

		// the reference is now non-empty and the property can be set
		entity2.setTestEntity(new TestEntity());
		Assert.assertTrue(ClassUtils.canSetProperty(entity2, "testEntity.name"));
		// neste property that does not exist
		Assert.assertFalse(ClassUtils.canSetProperty(entity2, "testEntity.phone"));
	}

	@Test
	public void testForClass() {
		Class<?> clazz = ClassUtils.forClass("com.ocs.dynamo.domain.TestEntity");
		Assert.assertNotNull(clazz);
	}

	@Test
	public void testForClassBogus() {
		Class<?> clazz = ClassUtils.forClass("com.ocs.dynamo.domain.TestEntityZ");
		Assert.assertNull(clazz);
	}

	@Test
	public void testForClassNull() {
		Class<?> clazz = ClassUtils.forClass("");
		Assert.assertNull(clazz);
	}

	@Test
	public void testGetAnnotationOnField() {
		Field fieldOne = ClassUtils.getField(TestObject.class, "fieldOne");

		// retrieve an annotation from a field
		Attribute attribute = ClassUtils.getAnnotationOnField(fieldOne, Attribute.class);
		Assert.assertNotNull(attribute);
		Assert.assertEquals("desc", attribute.description());

		// retrieve the annotation from the class and the field name
		attribute = ClassUtils.getAnnotationOnField(TestObject.class, "fieldOne", Attribute.class);
		Assert.assertEquals("desc", attribute.description());

		String desc = (String) ClassUtils.getAnnotationAttributeValue(fieldOne, Attribute.class,
		        "description");
		Assert.assertEquals("desc", desc);

		// check for null
		Entity entity = ClassUtils.getAnnotationOnField(fieldOne, Entity.class);
		Assert.assertNull(entity);
	}

	/**
	 * Test that retrieving an annotation from a getter method also works
	 */
	@Test
	public void testGetAnnotationOnMethod() {
		Attribute attribute = ClassUtils.getAnnotationOnMethod(TestObject.class, "fieldThree",
		        Attribute.class);
		Assert.assertNotNull(attribute);
		Assert.assertEquals("Bert", attribute.displayName());
	}

	@Test
	public void testGetBytes() {
		TestEntity entity = new TestEntity();
		entity.setSomeBytes(new byte[] { 1, 2, 3 });

		byte[] bytes = ClassUtils.getBytes(entity, "someBytes");
		Assert.assertNotNull(bytes);
		Assert.assertEquals(3, bytes.length);

		ClassUtils.setBytes(new byte[] { 2, 3, 4 }, entity, "someBytes");
		bytes = entity.getSomeBytes();
		Assert.assertEquals(2, bytes[0]);
		Assert.assertEquals(3, bytes[1]);
		Assert.assertEquals(4, bytes[2]);

		ClassUtils.setBytes(null, entity, "someBytes");
		Assert.assertNull(entity.getSomeBytes());
	}

	/**
	 * Basic test case for retrieving a field from a class
	 */
	@Test
	public void testGetField() {

		Assert.assertNull(ClassUtils.getField(TestObject.class, "bogus"));

		Field fieldOne = ClassUtils.getField(TestObject.class, "fieldOne");
		Assert.assertEquals(Integer.class, fieldOne.getType());

		Field fieldTwo = ClassUtils.getField(TestObject.class, "fieldTwo");
		Assert.assertEquals(String.class, fieldTwo.getType());
	}

	/**
	 * Verify that getting a field also works for a super class
	 */
	@Test
	public void testGetFieldRecursive() {
		Field fieldOne = ClassUtils.getField(TestObject2.class, "fieldOne");
		Assert.assertEquals(Integer.class, fieldOne.getType());
	}

	@Test
	public void testGetFieldValue() {
		TestEntity entity = new TestEntity();
		entity.setAge(12L);
		Assert.assertEquals(12L, ClassUtils.getFieldValue(entity, "age"));
	}

	@Test
	public void testGetFieldValueAsString() {
		TestEntity entity = new TestEntity();
		entity.setAge(12L);

		Assert.assertEquals("12", ClassUtils.getFieldValueAsString(entity, "age"));

		entity.setAge(null);
		Assert.assertNull(ClassUtils.getFieldValueAsString(entity, "age"));
	}

	@Test(expected = OCSRuntimeException.class)
	public void testGetFieldValueDoesntExists() {
		TestEntity entity = new TestEntity();
		entity.setAge(12L);
		ClassUtils.getFieldValue(entity, "age2");
	}

	@Test
	public void testGetFieldValueRecursive() {
		TestEntity entity = new TestEntity();
		entity.setAge(12L);

		TestEntity2 entity2 = new TestEntity2();
		entity2.setTestEntity(entity);

		Assert.assertEquals(12L, ClassUtils.getFieldValue(entity2, "testEntity.age"));
	}

	@Test
	public void testGetGetterMethod() {

		Method method = ClassUtils.getGetterMethod(TestEntity.class, "age");
		Assert.assertNotNull(method);

		// non existing method
		Method method3 = ClassUtils.getGetterMethod(TestEntity.class, "age2");
		Assert.assertNull(method3);

	}

	@Test
	public void testGetMaxLength() {
		Assert.assertEquals(-1, ClassUtils.getMaxLength(TestObject.class, "fieldOne"));
		Assert.assertEquals(123, ClassUtils.getMaxLength(TestObject.class, "fieldTwo"));
	}

	/**
	 * Basic test case for retrieving a getter method from a class
	 */
	@Test
	public void testGetMethod() {

		Assert.assertNull(ClassUtils.getGetterMethod(TestObject.class, "bogus"));

		Method method = ClassUtils.getGetterMethod(TestObject.class, "fieldOne");
		Assert.assertEquals(Integer.class, method.getReturnType());

		method = ClassUtils.getGetterMethod(TestObject.class, "fieldTwo");
		Assert.assertEquals(String.class, method.getReturnType());
	}

	@Test
	public void testGetResolvedType() {
		Class<?> type = ClassUtils.getResolvedType(TestObject3.class, "objects", 0);
		Assert.assertNotNull(type);
		Assert.assertEquals(TestObject.class, type);

		type = ClassUtils.getResolvedType(TestObject3.class, "indexedObjects", 0);
		Assert.assertNotNull(type);
		Assert.assertEquals(Integer.class, type);
		type = ClassUtils.getResolvedType(TestObject3.class, "indexedObjects", 1, 0);
		Assert.assertNotNull(type);
		Assert.assertEquals(TestObject.class, type);
	}

	@Test
	public void testHasMethod() {
		TestEntity entity = new TestEntity();
		Assert.assertTrue(ClassUtils.hasMethod(entity, "getAge"));
		Assert.assertTrue(ClassUtils.hasMethod(entity, "getSomeBoolean"));

		// check is case sensitive
		Assert.assertFalse(ClassUtils.hasMethod(entity, "getAGe"));
		// must provide the full method name
		Assert.assertFalse(ClassUtils.hasMethod(entity, "age"));
	}

	@Test
	public void testSetFieldValue() {
		TestEntity entity = new TestEntity();
		ClassUtils.setFieldValue(entity, "age", 12L);

		Assert.assertEquals(12L, entity.getAge().longValue());
	}

	@Test(expected = OCSRuntimeException.class)
	public void testSetFieldValueDoenstExist() {
		TestEntity entity = new TestEntity();
		ClassUtils.setFieldValue(entity, "age2", 12L);
	}

	@Test
	public void testSetFieldValueRecursive() {
		TestEntity entity = new TestEntity();
		TestEntity2 entity2 = new TestEntity2();
		entity2.setTestEntity(entity);

		ClassUtils.setFieldValue(entity2, "testEntity.age", 12L);

		Assert.assertEquals(12L, entity.getAge().longValue());
	}
}
