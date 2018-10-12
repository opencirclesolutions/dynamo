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
package com.ocs.dynamo.domain.model.impl;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSelect;
import com.ocs.dynamo.ui.component.TimeField;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.form.CollectionTable;
import com.ocs.dynamo.ui.converter.LocalDateTimeToDateConverter;
import com.ocs.dynamo.ui.converter.ZonedDateTimeToDateConverter;
import com.ocs.dynamo.ui.validator.URLValidator;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class ModelBasedFieldFactoryTest extends BaseIntegrationTest {

	@Inject
	private MessageService messageService;

	@Inject
	private EntityModelFactory factory;

	private ModelBasedFieldFactory<TestEntity> fieldFactory;

	private DecimalFormatSymbols symbols = DecimalFormatSymbols
			.getInstance(new Locale(SystemPropertyUtils.getDefaultLocale()));

	@Before
	public void setUp() {
		fieldFactory = new ModelBasedFieldFactory<TestEntity>(factory.getModel(TestEntity.class), messageService, false,
				false);
	}

	/**
	 * Test a text field
	 */
	@Test
	public void testTextField() {
		Object obj = fieldFactory.createField("name");
		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals("", tf.getNullRepresentation());
		Assert.assertNull(tf.getInputPrompt());
	}

	/**
	 * Test a URL field
	 */
	@Test
	public void testURLField() {
		Object obj = fieldFactory.createField("url");
		Assert.assertTrue(obj instanceof URLField);

		TextField tf = ((URLField) obj).getTextField();

		// check that a URL validator was added
		URLValidator validator = null;
		for (Validator v : tf.getValidators()) {
			if (v instanceof URLValidator) {
				validator = (URLValidator) v;
			}
		}
		Assert.assertNotNull(validator);
	}

	/**
	 * Test a text area
	 */
	@Test
	public void testTextArea() {
		Object obj = fieldFactory.createField("someTextArea");
		Assert.assertTrue(obj instanceof TextArea);
	}

	/**
	 * Test a long field
	 */
	@Test
	public void testLongField() {
		Object obj = fieldFactory.createField("age");
		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals(Long.class, tf.getConverter().getModelType());
	}

	/**
	 * Test an integer field
	 */
	@Test
	public void testIntegerField() {
		Object obj = fieldFactory.createField("someInt");
		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals(Integer.class, tf.getConverter().getModelType());
	}

	@Test
	public void testIntegerSlider() {
		Object obj = fieldFactory.createField("someIntSlider");
		Assert.assertTrue(obj instanceof Slider);

		Slider slider = (Slider) obj;
		Assert.assertEquals(Integer.class, slider.getConverter().getModelType());
		Assert.assertEquals(99, slider.getMin(), 0.001);
		Assert.assertEquals(175, slider.getMax(), 0.001);
	}

	@Test
	public void testLongSlider() {
		Object obj = fieldFactory.createField("someLongSlider");
		Assert.assertTrue(obj instanceof Slider);

		Slider slider = (Slider) obj;
		Assert.assertEquals(Long.class, slider.getConverter().getModelType());
	}

	/**
	 * Test an "element collection" field
	 */
	@Test
	public void testCollectionField() {
		Object obj = fieldFactory.createField("tags");
		Assert.assertTrue(obj instanceof CollectionTable);

		CollectionTable<?> ct = (CollectionTable<?>) obj;
		Assert.assertEquals(25, ct.getMaxLength().intValue());
	}

	/**
	 * Test a text field
	 */
	@Test
	public void testTextFieldValidating() {
		fieldFactory = ModelBasedFieldFactory.getValidatingInstance(factory.getModel(TestEntity.class), messageService);
		Object obj = fieldFactory.createField("name");

		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals("", tf.getNullRepresentation());
		Assert.assertNull(tf.getInputPrompt());
		Assert.assertFalse(tf.getValidators().isEmpty());
	}

	/**
	 * Test a text field that displays a BigDecimal
	 */
	@Test
	public void testBigDecimalField() {
		Object obj = fieldFactory.createField("discount");
		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals("", tf.getNullRepresentation());

		Assert.assertNotNull(tf.getConverter());

		BigDecimal dec = (BigDecimal) tf.getConverter().convertToModel("3" + symbols.getDecimalSeparator() + "43", null,
				null);
		Assert.assertEquals(new BigDecimal("3.43"), dec);
	}

	@Test
	public void testBigDecimalPercentageField() {
		Object obj = fieldFactory.createField("rate");
		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals("", tf.getNullRepresentation());

		Assert.assertNotNull(tf.getConverter());

		BigDecimal dec = (BigDecimal) tf.getConverter().convertToModel("3" + symbols.getDecimalSeparator() + "43%",
				null, null);
		Assert.assertEquals(new BigDecimal("3.43"), dec);

		Assert.assertEquals("3" + symbols.getDecimalSeparator() + "14%",
				tf.getConverter().convertToPresentation(BigDecimal.valueOf(3.14), null, null));
	}

	/**
	 * Test the creation of a date field and verify the date format is correctly
	 * set
	 */
	@Test
	public void testDateField() {
		Object obj = fieldFactory.createField("birthDate");
		Assert.assertTrue(obj instanceof DateField);

		DateField df = (DateField) obj;
		Assert.assertEquals("dd/MM/yyyy", df.getDateFormat());
	}

	/**
	 * Test the creation of a time field
	 */
	@Test
	public void testTimeField() {
		Object obj = fieldFactory.createField("someTime");
		Assert.assertTrue(obj instanceof TimeField);

		TimeField tf = (TimeField) obj;
		Assert.assertEquals(Resolution.MINUTE, tf.getResolution());
	}

	/**
	 * Test the creation of a time field
	 */
	@Test
	public void testZonedDateTimeField() {
		Object obj = fieldFactory.createField("zoned");
		Assert.assertTrue(obj instanceof DateField);

		DateField tf = (DateField) obj;
		Assert.assertEquals(Resolution.SECOND, tf.getResolution());
		Assert.assertTrue(tf.getConverter().getClass().isAssignableFrom(ZonedDateTimeToDateConverter.class));
	}

	@Test
	public void testLocalDateTimeField() {
		Object obj = fieldFactory.createField("registrationTime");
		Assert.assertTrue(obj instanceof DateField);

		DateField tf = (DateField) obj;
		Assert.assertEquals(Resolution.SECOND, tf.getResolution());
		Assert.assertTrue(tf.getConverter().getClass().isAssignableFrom(LocalDateTimeToDateConverter.class));
	}

	/**
	 * Test the creation of a date field and verify the date format is correctly
	 * set
	 */
	@Test
	public void testWeekField() {
		Object obj = fieldFactory.createField("birthWeek");
		Assert.assertTrue(obj instanceof TextField);

		TextField tf = (TextField) obj;
		Assert.assertEquals("", tf.getNullRepresentation());

		Assert.assertNotNull(tf.getConverter());

		Date d = (Date) tf.getConverter().convertToModel("2015-03", null, null);
		Assert.assertEquals(DateUtils.createDate("12012015"), d);
	}

	/**
	 * Test the creation of an enum field
	 */
	@Test
	public void testEnumField() {
		Object obj = fieldFactory.createField("someEnum");
		Assert.assertTrue(obj instanceof AbstractSelect);

		AbstractSelect select = (AbstractSelect) obj;
		Item item = select.getItem(TestEntity.TestEnum.A);
		Assert.assertNotNull(item);
		Assert.assertEquals("Value A", item.getItemProperty("Caption").getValue());

		Item itemB = select.getItem(TestEntity.TestEnum.B);
		Assert.assertNotNull(itemB);
		Assert.assertEquals("Value B", itemB.getItemProperty("Caption").getValue());
	}

	/**
	 * Test that in a search screen, a CombobBox is created for a boolean field
	 */
	@Test
	public void testSearchBooleanField() {
		fieldFactory = ModelBasedFieldFactory.getSearchInstance(factory.getModel(TestEntity.class), messageService);

		Field<?> field = fieldFactory.createField("someBoolean");
		Assert.assertTrue(field instanceof ComboBox);
	}

	/**
	 * Test that in a normal screen, a Checkbox is created for a boolean field
	 */
	@Test
	public void testNormalBooleanField() {
		fieldFactory = ModelBasedFieldFactory.getInstance(factory.getModel(TestEntity.class), messageService);

		Field<?> field = fieldFactory.createField("someBoolean");
		Assert.assertTrue(field instanceof CheckBox);
	}

	@Test
	public void testDoNotCreateReadonlyField() {
		ModelBasedFieldFactory<TestX> f = ModelBasedFieldFactory.getInstance(factory.getModel(TestX.class),
				messageService);
		Assert.assertNull(f.createField("readOnlyField"));

		// in search mode, it does not matter if the field is readonly
		f = ModelBasedFieldFactory.getSearchInstance(factory.getModel(TestX.class), messageService);
		Assert.assertNotNull(f.createField("readOnlyField"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructField() {
		EntityModel<TestEntity2> model = factory.getModel(TestEntity2.class);
		ModelBasedFieldFactory<TestEntity2> ff = ModelBasedFieldFactory.getInstance(model, messageService);

		// simple case - simply create the normal field
		Field<?> f = ff.constructField(model.getAttributeModel("name"), null, null);
		Assert.assertTrue(f instanceof TextField);
		Assert.assertEquals("myStyle", f.getStyleName());

		// default case - lookup field (no field filter)
		f = ff.constructField(model.getAttributeModel("testEntity"), null, null);
		Assert.assertTrue(f instanceof EntityLookupField);
		EntityLookupField<Integer, TestEntity> lf = (EntityLookupField<Integer, TestEntity>) f;
		Assert.assertNull(lf.getFilter());

		// field filter is propagated
		Map<String, com.vaadin.data.Container.Filter> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntity", new Compare.Equal("name", "John"));

		f = ff.constructField(model.getAttributeModel("testEntity"), fieldFilters, null);
		Assert.assertTrue(f instanceof EntityLookupField);
		lf = (EntityLookupField<Integer, TestEntity>) f;
		Assert.assertNotNull(lf.getFilter());

		fieldFilters = new HashMap<>();
		fieldFilters.put("testEntityAlt", new Compare.Equal("name", "John"));

		f = ff.constructField(model.getAttributeModel("testEntityAlt"), fieldFilters, null);
		Assert.assertTrue(f instanceof QuickAddEntityComboBox);
		QuickAddEntityComboBox<Integer, TestEntity> df = (QuickAddEntityComboBox<Integer, TestEntity>) f;
		Assert.assertEquals(SelectMode.FILTERED, df.getComboBox().getSelectMode());

		f = ff.constructField(model.getAttributeModel("testEntityAlt2"), fieldFilters, null);
		Assert.assertTrue(f instanceof QuickAddListSelect);
	}

	@SuppressWarnings("unused")
	private class TestX extends AbstractEntity<Integer> {

		private static final long serialVersionUID = 2993052752064838180L;

		private Integer id;

		@Attribute(editable = EditableType.READ_ONLY)
		private String readOnlyField;

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public void setId(Integer id) {
			this.id = id;
		}

		public String getReadOnlyField() {
			return readOnlyField;
		}

		public void setReadOnlyField(String readOnlyField) {
			this.readOnlyField = readOnlyField;
		}

	}
}
