package com.ocs.dynamo.ui.component;

import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.test.BaseMockitoTest;

public class TimeFieldTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Test
	public void testCreateAndSetValue() {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("someTime");
		TimeField tf = new TimeField(am);

		Assert.assertNotNull(tf.getHourSelect());
		Assert.assertNotNull(tf.getMinuteSelect());

		// set value
		tf.setValue(LocalTime.of(8, 7));
		Assert.assertEquals(8, tf.getHourSelect().getValue().intValue());
		Assert.assertEquals(7, tf.getMinuteSelect().getValue().intValue());

		// clear value
		tf.setValue(null);
		Assert.assertNull(tf.getHourSelect().getValue());
		Assert.assertEquals(0, tf.getMinuteSelect().getValue().intValue());
	}
}
