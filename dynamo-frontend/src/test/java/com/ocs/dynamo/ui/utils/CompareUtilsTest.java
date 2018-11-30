package com.ocs.dynamo.ui.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

public class CompareUtilsTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService = new MessageServiceImpl();

	@Override
	public void setUp() {
		super.setUp();
		MockUtil.mockMessageService(messageService);
	}

	@Test
	public void testCompare() {
		TestEntity e1 = new TestEntity();
		TestEntity e2 = new TestEntity();

		// there are no changes
		List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory,
				messageService);
		Assert.assertEquals(0, changes.size());

		e1.setAge(12L);
		changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
		Assert.assertEquals(1, changes.size());
		Assert.assertEquals("ocs.value.changed", changes.get(0));

		e1.setAge(null);
		e2.setName("Kevin");
		changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService);
		Assert.assertEquals(1, changes.size());
		Assert.assertEquals("ocs.value.changed", changes.get(0));
	}

	@Test
	public void testCompare_Ignore() {
		TestEntity e1 = new TestEntity();
		TestEntity e2 = new TestEntity();

		e1.setName("Bob");

		// name has changed
		List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory,
				messageService);
		Assert.assertEquals(1, changes.size());

		// ignore the name change
		changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, messageService, "name");
		Assert.assertEquals(0, changes.size());
	}

	@Test
	public void testCompare_CollectionRemove() {
		TestEntity e1 = new TestEntity();
		TestEntity e2 = new TestEntity();

		e1.addTestEntity2(new TestEntity2());

		List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory,
				messageService);
		Assert.assertEquals(1, changes.size());
		Assert.assertEquals("ocs.value.removed", changes.get(0));
	}

	@Test
	public void testCompare_CollectionAdd() {
		TestEntity e1 = new TestEntity();
		TestEntity e2 = new TestEntity();

		e2.addTestEntity2(new TestEntity2());

		List<String> changes = CompareUtils.compare(e1, e2, factory.getModel(TestEntity.class), factory, 
				messageService);
		Assert.assertEquals(1, changes.size());
		Assert.assertEquals("ocs.value.added", changes.get(0));
	}
}
