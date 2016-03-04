package com.ocs.dynamo.ui.grid;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;

public class IntegerSumGeneratorTest {

	@Test
	public void test() {

		TestY test = new TestY();
		test.setWeek1_prop(12);
		test.setWeek2_prop(13);
		test.setWeek3_prop(14);
		// this value will NOT be added
		test.setWeek4_prop(new BigDecimal(15));
		// this value will NOT be added
		test.setSkip(15);

		BeanItemContainer<TestY> container = new BeanItemContainer<TestY>(TestY.class,
		        Lists.newArrayList(test));
		Table table = new Table("", container);

		IntegerSumGenerator generator = new IntegerSumGenerator("prop");
		Assert.assertEquals(Integer.class, generator.getType());

		Object itemId = table.getItemIds().iterator().next();

		Integer value = generator.getValue(table.getItem(itemId), itemId, null);
		Assert.assertEquals(39, value.intValue());
	}

}
