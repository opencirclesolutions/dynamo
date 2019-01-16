package com.ocs.dynamo.domain.model.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.hibernate.annotations.Check;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.vaadin.teemu.switchui.Switch;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.component.ElementCollectionGrid;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.FancyListSelect;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSelect;
import com.ocs.dynamo.ui.component.QuickAddListSingleSelect;
import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
import com.ocs.dynamo.ui.component.TimeField;
import com.ocs.dynamo.ui.component.TokenFieldSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FieldFactoryImplTest extends BaseIntegrationTest {

	@Inject
	private EntityModelFactory factory;

	private FieldFactory fieldFactory;

	@Before
	public void setUp() {
		fieldFactory = FieldFactory.getInstance();
	}

	private AbstractComponent constructField(String name, boolean search) {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setSearch(search);
		return fieldFactory.constructField(context);
	}

	private AbstractComponent constructField(String name, String entityModelRef) {
		EntityModel<TestEntity> em = factory.getModel(entityModelRef, TestEntity.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setSearch(false);
		return fieldFactory.constructField(context);
	}

	private AbstractComponent constructField2(String name, boolean search, boolean viewMode) {
		EntityModel<TestEntity2> em = factory.getModel(TestEntity2.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setSearch(search).setViewMode(viewMode);
		return fieldFactory.constructField(context);
	}

	private AbstractComponent constructField2(String name, Map<String, SerializablePredicate<?>> fieldFilters) {
		EntityModel<TestEntity2> em = factory.getModel(TestEntity2.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setFieldFilters(fieldFilters);
		return fieldFactory.constructField(context);
	}

	/**
	 * Test a text field
	 */
	@Test
	public void testTextField() {

		AbstractComponent ac = constructField("name", false);
		Assert.assertTrue(ac instanceof TextField);

		TextField tf = (TextField) ac;
		Assert.assertEquals("Name", tf.getCaption());
	}

	/**
	 * Test a URL field
	 */
	@Test
	public void testURLField() {
		AbstractComponent ac = constructField("url", false);
		Assert.assertTrue(ac instanceof URLField);
	}

	/**
	 * Test a text area
	 */
	@Test
	public void testTextArea() {
		AbstractComponent ac = constructField("someTextArea", false);
		Assert.assertTrue(ac instanceof TextArea);
		TextArea ta = (TextArea) ac;
		Assert.assertEquals(4, ta.getRows());
	}

	/**
	 * Test a long field
	 */
	@Test
	public void testLongField() {
		AbstractComponent ac = constructField("age", false);
		Assert.assertTrue(ac instanceof TextField);
	}

	/**
	 * Test an integer field
	 */
	@Test
	public void testIntegerField() {
		AbstractComponent ac = constructField("age", false);
		Assert.assertTrue(ac instanceof TextField);
	}

	@Test
	public void testIntegerSlider() {
		AbstractComponent ac = constructField("someIntSlider", false);
		Assert.assertTrue(ac instanceof Slider);

		Slider slider = (Slider) ac;
		Assert.assertEquals(99, slider.getMin(), 0.001);
		Assert.assertEquals(175, slider.getMax(), 0.001);

		Binder<TestEntity> binder = new BeanValidationBinder<>(TestEntity.class);
		TestEntity t1 = new TestEntity();
		binder.setBean(t1);

		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		fieldFactory.addConvertersAndValidators(binder.forField(slider), em.getAttributeModel("someIntSlider"), null);

	}

	@Test
	public void testLongSlider() {
		AbstractComponent ac = constructField("someLongSlider", false);
		Assert.assertTrue(ac instanceof Slider);
	}

	/**
	 * Test an "element collection" field
	 */
	@Test
	public void testCollectionField() {
		AbstractComponent ac = constructField("tags", false);
		Assert.assertTrue(ac instanceof ElementCollectionGrid);

		ElementCollectionGrid<?, ?, ?> ct = (ElementCollectionGrid<?, ?, ?>) ac;
		Assert.assertEquals(25, ct.getMaxLength().intValue());
	}

	/**
	 * element collection field in search mode (fails due to h2 problem?)
	 */
	@Test
	@Ignore
	public void testCollectionFieldSearch() {
		AbstractComponent ac = constructField("tags", true);
		Assert.assertTrue(ac instanceof SimpleTokenFieldSelect);
	}

	/**
	 * Test a text field that displays a BigDecimal
	 */
	@Test
	public void testBigDecimalField() {
		AbstractComponent ac = constructField("discount", false);
		Assert.assertTrue(ac instanceof TextField);
	}

	/**
	 * Test that a text field with percentage support is generated
	 */
	@Test
	public void testBigDecimalPercentageField() {
		AbstractComponent ac = constructField("rate", false);
		Assert.assertTrue(ac instanceof TextField);

		// check that a blur listener is added
		TextField tf = (TextField) ac;
		Assert.assertEquals(1, tf.getListeners(BlurEvent.class).size());
	}

	@Test
	public void testBigDecimalCurrencyField() {
		AbstractComponent ac = constructField2("currency", false, false);
		Assert.assertTrue(ac instanceof TextField);

		// check that a blur listener is added
		TextField tf = (TextField) ac;
		Assert.assertEquals(1, tf.getListeners(BlurEvent.class).size());
	}

	/**
	 * Test the creation of a date field and verify the date format is correctly set
	 */
	@Test
	public void testDateField() {
		AbstractComponent ac = constructField("birthDate", false);
		Assert.assertTrue(ac instanceof DateField);

		DateField df = (DateField) ac;
		Assert.assertEquals("dd/MM/yyyy", df.getDateFormat());
	}

	/**
	 * Test the creation of a time field
	 */
	@Test
	public void testTimeField() {
		AbstractComponent ac = constructField("someTime", false);
		Assert.assertTrue(ac instanceof TimeField);
	}

	/**
	 * Test the creation of a zoned date time field
	 */
	@Test
	public void testZonedDateTimeField() {
		AbstractComponent ac = constructField("zoned", false);
		Assert.assertTrue(ac instanceof DateTimeField);

		DateTimeField tf = (DateTimeField) ac;
		Assert.assertEquals("dd-MM-yyyy HH:mm:ssZ", tf.getDateFormat());
	}

	/**
	 * Test the creation of date field for searching on date only
	 */
	@Test
	public void testSearchDateOnly() {
		AbstractComponent ac = constructField2("searchDateOnly", true, false);
		Assert.assertTrue(ac instanceof DateField);

		DateField tf = (DateField) ac;
		Assert.assertEquals("dd-MM-yyyy", tf.getDateFormat());
	}

	/**
	 * Test the creation of a local date time field
	 */
	@Test
	public void testLocalDateTimeField() {
		AbstractComponent ac = constructField("registrationTime", false);
		Assert.assertTrue(ac instanceof DateTimeField);
	}

	/**
	 * Test the creation of a date field and verify the date format is correctly set
	 */
	@Test
	public void testWeekField() {
		AbstractComponent ac = constructField("birthWeek", false);
		Assert.assertTrue(ac instanceof TextField);
	}

	/**
	 * Test the creation of an enum field
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testEnumField() {
		AbstractComponent ac = constructField("someEnum", false);
		Assert.assertTrue(ac instanceof ComboBox);

		ComboBox<TestEnum> cb = (ComboBox<TestEnum>) ac;
		ListDataProvider<TestEnum> ldp = (ListDataProvider<TestEnum>) cb.getDataProvider();

		Assert.assertTrue(ldp.getItems().contains(TestEnum.A));
		Assert.assertTrue(ldp.getItems().contains(TestEnum.B));
		Assert.assertTrue(ldp.getItems().contains(TestEnum.C));
	}

	/**
	 * E-mail field
	 */
	@Test
	public void testEmail() {
		AbstractComponent ac = constructField2("email", false, false);
		Assert.assertTrue(ac instanceof TextField);
	}

	/**
	 * Test that no field is generated in read-only mode
	 */
	@Test
	public void testReadOnlyNoField() {
		AbstractComponent ac = constructField2("readOnly", false, false);
		Assert.assertNull(ac);
	}

	/**
	 * Test that in search mode a field is generated even for a read-only property
	 */
	@Test
	public void testReadOnlySearch() {
		AbstractComponent ac = constructField2("readOnly", true, false);
		Assert.assertTrue(ac instanceof TextField);
	}

	/**
	 * Test that even in read only mode, an URL field is created
	 */
	@Test
	public void testReadOnlyUrl() {
		AbstractComponent ac = constructField2("url", false, false);
		Assert.assertTrue(ac instanceof URLField);
	}

	/**
	 * Test that in a search screen, a CombobBox is created for a boolean field
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void tesSearchBooleanField() {
		AbstractComponent ac = constructField("someBoolean", true);
		Assert.assertTrue(ac instanceof ComboBox);

		ComboBox<Boolean> cb = (ComboBox<Boolean>) ac;
		ListDataProvider<Boolean> ldp = (ListDataProvider<Boolean>) cb.getDataProvider();

		Assert.assertTrue(ldp.getItems().contains(Boolean.TRUE));
		Assert.assertTrue(ldp.getItems().contains(Boolean.FALSE));
	}

	public void testNormalBooleanField() {
		AbstractComponent ac = constructField("someBoolean", false);
		Assert.assertTrue(ac instanceof Check);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityLookupField() {
		AbstractComponent ac = constructField2("testEntity", false, false);
		Assert.assertTrue(ac instanceof EntityLookupField);

		EntityLookupField<Integer, TestEntity> lf = (EntityLookupField<Integer, TestEntity>) ac;
		Assert.assertNull(lf.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityLookupFieldWithFieldFilter() {
		Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntity", new EqualsPredicate<>("name", "Bob"));
		AbstractComponent ac = constructField2("testEntity", fieldFilters);
		Assert.assertTrue(ac instanceof EntityLookupField);

		EntityLookupField<Integer, TestEntity> lf = (EntityLookupField<Integer, TestEntity>) ac;
		Assert.assertNotNull(lf.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityComboBox() {
		AbstractComponent ac = constructField2("testEntityAlt", false, false);
		Assert.assertTrue(ac instanceof QuickAddEntityComboBox);

		QuickAddEntityComboBox<Integer, TestEntity> cb = (QuickAddEntityComboBox<Integer, TestEntity>) ac;
		Assert.assertNull(cb.getFilter());
	}

	/**
	 * Test that in search mode a token field is selected if multipe search is
	 * specified
	 */
	@Test
	public void testConstructEntityComboBoxMultipleSearch() {
		EntityModel<TestEntity2> em = factory.getModel("TestEntity2Multi", TestEntity2.class);
		FieldFactoryContext context = FieldFactoryContext.create()
				.setAttributeModel(em.getAttributeModel("testEntityAlt")).setSearch(true);

		AbstractComponent ac = fieldFactory.constructField(context);
		Assert.assertTrue(ac instanceof TokenFieldSelect);
	}

	/**
	 * Test list select (and number of rows)
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testConstructListSingleSelect() {
		AbstractComponent ac = constructField2("testEntityAlt2", false, false);
		Assert.assertTrue(ac instanceof QuickAddListSingleSelect);

		QuickAddListSingleSelect<Integer, TestEntity> ls = (QuickAddListSingleSelect<Integer, TestEntity>) ac;
		Assert.assertNull(ls.getFilter());
		Assert.assertEquals(6, ls.getListSelect().getVisibleItemCount());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructListSingleSelectWithFilter() {
		Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntityAlt2", new EqualsPredicate<>("name", "Bob"));
		AbstractComponent ac = constructField2("testEntityAlt2", fieldFilters);
		Assert.assertTrue(ac instanceof QuickAddListSingleSelect);

		QuickAddListSingleSelect<Integer, TestEntity> ls = (QuickAddListSingleSelect<Integer, TestEntity>) ac;
		Assert.assertNotNull(ls.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityComboBoxWithFieldFilter() {

		Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntityAlt", new EqualsPredicate<>("name", "Bob"));
		AbstractComponent ac = constructField2("testEntityAlt", fieldFilters);
		Assert.assertTrue(ac instanceof QuickAddEntityComboBox);

		QuickAddEntityComboBox<Integer, TestEntity> cb = (QuickAddEntityComboBox<Integer, TestEntity>) ac;
		Assert.assertNotNull(cb.getFilter());
	}

	/**
	 * Test that a link field is constructed for a navigable property in view mode
	 */
	@Test
	public void testConstructLinkField() {
		AbstractComponent ac = constructField2("testEntity", false, true);
		Assert.assertTrue(ac instanceof InternalLinkField);
	}

	/**
	 * Test that a simple token field select component is rendered in search mode
	 */
	@Test
	public void testConstructSimpleTokenField() {
		AbstractComponent ac = constructField2("basicToken", true, false);
		Assert.assertTrue(ac instanceof SimpleTokenFieldSelect);
	}

	/**
	 * Test that in edit mode just a text field is constructed
	 */
	@Test
	public void testConstructSimpleTokenField2() {
		AbstractComponent ac = constructField2("basicToken", false, false);
		Assert.assertTrue(ac instanceof TextField);
	}

	/**
	 * Test that a switch field is constructed
	 */
	@Test
	public void testConstructSwitch() {
		AbstractComponent ac = constructField2("switchBool", false, false);
		Assert.assertTrue(ac instanceof Switch);
	}

	/**
	 * Token field select as default for detail relation
	 */
	@Test
	public void testDetailTokenFieldSelect() {
		AbstractComponent ac = constructField("testEntities", false);
		Assert.assertTrue(ac instanceof TokenFieldSelect);

		TokenFieldSelect<?, ?> tf = (TokenFieldSelect<?, ?>) ac;
		Assert.assertNull(tf.getFilter());
	}

	/**
	 * Fancy list select for detail collection
	 */
	@Test
	public void testDetailFancyList() {
		AbstractComponent ac = constructField("testEntities", "TestEntityFancyList");
		Assert.assertTrue(ac instanceof FancyListSelect);

		FancyListSelect<?, ?> fl = (FancyListSelect<?, ?>) ac;
		Assert.assertNull(fl.getFilter());
		Assert.assertEquals("Test Entities", fl.getCaption());
		Assert.assertNull(fl.getAddButton());
	}

	/**
	 * List select for detail collection
	 */
	@Test
	public void testDetailList() {
		AbstractComponent ac = constructField("testEntities", "TestEntityListSelect");
		Assert.assertTrue(ac instanceof QuickAddListSelect);

		QuickAddListSelect<?, ?> fl = (QuickAddListSelect<?, ?>) ac;
		Assert.assertNull(fl.getFilter());
		Assert.assertEquals("Test Entities", fl.getCaption());
		Assert.assertNull(fl.getAddButton());
	}

	/**
	 * Lookup field for detail collection
	 */
	@Test
	public void testDetailLookup() {
		AbstractComponent ac = constructField("testEntities", "TestEntityLookup");
		Assert.assertTrue(ac instanceof EntityLookupField);

		EntityLookupField<?, ?> fl = (EntityLookupField<?, ?>) ac;
		Assert.assertNull(fl.getFilter());
		Assert.assertEquals("Test Entities", fl.getCaption());
		Assert.assertNull(fl.getAddButton());
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
