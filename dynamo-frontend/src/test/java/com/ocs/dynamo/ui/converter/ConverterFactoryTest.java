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
package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.data.Converter;
import com.vaadin.data.converter.StringToDoubleConverter;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.converter.StringToLongConverter;

public class ConverterFactoryTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService;

	private EntityModel<TestEntity> em;

	@Before
	public void setupTableUtilsTest() throws NoSuchFieldException {
		em = factory.getModel(TestEntity.class);
		ReflectionTestUtils.setField(factory, "messageService", messageService);
	}

	@Test
	public void testGetConverterFor() {
		Converter<?, ?> cv = ConverterFactory.createConverterFor(Integer.class, em.getAttributeModel("someInt"), true);
		Assert.assertTrue(cv instanceof GroupingStringToIntegerConverter);

		cv = ConverterFactory.createConverterFor(Long.class, em.getAttributeModel("age"), true);
		Assert.assertTrue(cv instanceof GroupingStringToLongConverter);

		cv = ConverterFactory.createConverterFor(BigDecimal.class, em.getAttributeModel("rate"), true);
		Assert.assertTrue(cv instanceof BigDecimalConverter);
	}

	@Test
	public void testCreateBigDecimalConverter() {
		BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(false, false, false, 2, null);
		Assert.assertFalse(cv instanceof PercentageBigDecimalConverter);
	}

	@Test
	public void testCreateBigDecimalConverter2() {
		BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(false, true, false, 2, null);
		Assert.assertTrue(cv instanceof PercentageBigDecimalConverter);
	}

	@Test
	public void testCreateBigDecimalConverter3() {
		BigDecimalConverter cv = ConverterFactory.createBigDecimalConverter(true, false, false, 2, "EUR");
		Assert.assertTrue(cv instanceof CurrencyBigDecimalConverter);
	}

	@Test
	public void testCreateIntegerConverter1() {
		StringToIntegerConverter cv = ConverterFactory.createIntegerConverter(true, false);
		Assert.assertTrue(cv instanceof GroupingStringToIntegerConverter);
	}

	@Test
	public void testCreateIntegerConverter2() {
		StringToIntegerConverter cv = ConverterFactory.createIntegerConverter(true, true);
		Assert.assertTrue(cv instanceof PercentageIntegerConverter);
	}

	@Test
	public void testCreateLongConverter1() {
		StringToLongConverter cv = ConverterFactory.createLongConverter(true, false);
		Assert.assertTrue(cv instanceof GroupingStringToLongConverter);
	}

	@Test
	public void testCreateLongConverter() {
		StringToLongConverter cv = ConverterFactory.createLongConverter(true, true);
		Assert.assertTrue(cv instanceof PercentageLongConverter);
	}

	@Test
	public void testCreateDoubleConverter1() {
		GroupingStringToDoubleConverter cv = (GroupingStringToDoubleConverter) ConverterFactory
				.createDoubleConverter(false, false, false, 2, null);
		Assert.assertFalse(cv instanceof PercentageDoubleConverter);
	}

	@Test
	public void testCreateDoubleConverter2() {
		StringToDoubleConverter cv = ConverterFactory.createDoubleConverter(false, true, false, 2, null);
		Assert.assertTrue(cv instanceof PercentageDoubleConverter);
	}

	@Test
	public void testCreateDoubleConverter3() {
		StringToDoubleConverter cv = ConverterFactory.createDoubleConverter(true, false, false, 2, null);
		Assert.assertTrue(cv instanceof CurrencyDoubleConverter);
	}
}
