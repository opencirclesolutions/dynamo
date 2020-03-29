package com.ocs.dynamo.domain.model.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.mvysny.kaributesting.v10.MockVaadin;
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
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.component.ElementCollectionGrid;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSingleSelect;
import com.ocs.dynamo.ui.component.QuickAddTokenSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.component.ZonedDateTimePicker;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

public class FieldFactoryImplTest extends FrontendIntegrationTest {

	@Autowired
	private EntityModelFactory factory;

	private FieldFactory fieldFactory;

	@BeforeEach
	public void setUp() {
		MockVaadin.setup();
		fieldFactory = FieldFactory.getInstance();
	}

	private Component constructField(String name, boolean search) {
		EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setSearch(search);
		return fieldFactory.constructField(context);
	}

	private Component constructField(String name, String entityModelRef) {
		EntityModel<TestEntity> em = factory.getModel(entityModelRef, TestEntity.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setSearch(false);
		return fieldFactory.constructField(context);
	}

	private Component constructField2(String name, boolean search, boolean viewMode) {
		return constructField2(name, search, viewMode, false);
	}

	private Component constructField2(String name, boolean search, boolean viewMode, boolean grid) {
		EntityModel<TestEntity2> em = factory.getModel(TestEntity2.class);
		FieldFactoryContext context = FieldFactoryContext.create().setAttributeModel(em.getAttributeModel(name))
				.setSearch(search).setViewMode(viewMode).setEditableGrid(grid);
		return fieldFactory.constructField(context);
	}

	private Component constructField2(String name, Map<String, SerializablePredicate<?>> fieldFilters) {
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

		Component ac = constructField("name", false);
		assertTrue(ac instanceof TextField);

		TextField tf = (TextField) ac;
		assertEquals("Name", tf.getLabel());
	}

	/**
	 * Test a URL field
	 */
	@Test
	public void testURLField() {
		Component ac = constructField("url", false);
		assertTrue(ac instanceof URLField);
	}

	/**
	 * Test a text area
	 */
	@Test
	public void testTextArea() {
		Component ac = constructField("someTextArea", false);
		assertTrue(ac instanceof TextArea);
	}

	/**
	 * Test a long field
	 */
	@Test
	public void testLongField() {
		Component ac = constructField("age", false);
		assertTrue(ac instanceof TextField);
	}

	/**
	 * Test an integer field
	 */
	@Test
	public void testIntegerField() {
		Component ac = constructField("age", false);
		assertTrue(ac instanceof TextField);
	}

//    @Test
//    public void testIntegerSlider() {
//        Component ac = constructField("someIntSlider", false);
//        assertTrue(ac instanceof Slider);
//
//        Slider slider = (Slider) ac;
//        assertEquals(99, slider.getMin(), 0.001);
//        assertEquals(175, slider.getMax(), 0.001);
//
//        Binder<TestEntity> binder = new BeanValidationBinder<>(TestEntity.class);
//        TestEntity t1 = new TestEntity();
//        binder.setBean(t1);
//
//        EntityModel<TestEntity> em = factory.getModel(TestEntity.class);
//        fieldFactory.addConvertersAndValidators(binder.forField(slider), em.getAttributeModel("someIntSlider"), null);
//    }

//    @Test
//    public void testLongSlider() {
//        Component ac = constructField("someLongSlider", false);
//        assertTrue(ac instanceof Slider);
//    }

	/**
	 * Test an "element collection" field
	 */
	@Test
	public void testCollectionField() {
		Component ac = constructField("tags", false);
		assertTrue(ac instanceof ElementCollectionGrid);

		ElementCollectionGrid<?, ?, ?> ct = (ElementCollectionGrid<?, ?, ?>) ac;
		assertEquals(25, ct.getMaxLength().intValue());
	}

	/**
	 * Test a text field that displays a BigDecimal
	 */
	@Test
	public void testBigDecimalField() {
		Component ac = constructField("discount", false);
		assertTrue(ac instanceof TextField);
	}

	/**
	 * Test that a text field with percentage support is generated
	 */
	@Test
	public void testBigDecimalPercentageField() {
		Component ac = constructField("rate", false);
		assertTrue(ac instanceof TextField);
	}

	@Test
	public void testBigDecimalCurrencyField() {
		Component ac = constructField2("currency", false, false);
		assertTrue(ac instanceof TextField);
	}

	/**
	 * Test the creation of a date field and verify the date format is correctly set
	 */
	@Test
	public void testDateField() {
		Component ac = constructField("birthDate", false);
		assertTrue(ac instanceof DatePicker);
	}

	/**
	 * Test the creation of a zoned date time field
	 */
	@Test
	public void testZonedDateTimeField() {
		Component ac = constructField("zoned", false);
		assertTrue(ac instanceof ZonedDateTimePicker);
	}

	/**
	 * Test the creation of date field for searching on date only
	 */
	@Test
	public void testSearchDateOnly() {
		Component ac = constructField2("searchDateOnly", true, false);
		assertTrue(ac instanceof DatePicker);
	}

	/**
	 * Test the creation of a local date time field
	 */
	@Test
	public void testLocalTimeField() {
		Component ac = constructField("registrationTime", false);
		assertTrue(ac instanceof TimePicker);
	}

	/**
	 * Test the creation of a date field and verify the date format is correctly set
	 */
	@Test
	public void testWeekField() {
		Component ac = constructField("birthWeek", false);
		assertTrue(ac instanceof TextField);
	}

	/**
	 * Test the creation of an enum field
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testEnumField() {
		Component ac = constructField("someEnum", false);
		assertTrue(ac instanceof ComboBox);

		ComboBox<TestEnum> cb = (ComboBox<TestEnum>) ac;
		ListDataProvider<TestEnum> ldp = (ListDataProvider<TestEnum>) cb.getDataProvider();

		assertTrue(ldp.getItems().contains(TestEnum.A));
		assertTrue(ldp.getItems().contains(TestEnum.B));
		assertTrue(ldp.getItems().contains(TestEnum.C));
	}

	/**
	 * E-mail field
	 */
	@Test
	public void testEmail() {
		Component ac = constructField2("email", false, false);
		assertTrue(ac instanceof TextField);

		TextField tf = (TextField) ac;

		Binder<TestEntity2> binder = new BeanValidationBinder<>(TestEntity2.class);
		TestEntity2 t2 = new TestEntity2();
		binder.setBean(t2);
		EntityModel<TestEntity2> em = factory.getModel(TestEntity2.class);

		fieldFactory.addConvertersAndValidators(binder.forField(tf), em.getAttributeModel("email"), null, null, null);
	}

	/**
	 * Test that no field is generated in read-only mode
	 */
	@Test
	public void testReadOnlyNoField() {
		Component ac = constructField2("readOnly", false, false);
		assertNull(ac);
	}

	/**
	 * Test that in search mode a field is generated even for a read-only property
	 */
	@Test
	public void testReadOnlySearch() {
		Component ac = constructField2("readOnly", true, false);
		assertTrue(ac instanceof TextField);
	}

	/**
	 * Test that in read only mode, no field is created any more
	 */
	@Test
	public void testReadOnlyUrl() {
		Component ac = constructField2("url", false, false);
		assertNull(ac);
	}

	/**
	 * Test that in a search screen, a CombobBox is created for a boolean field
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testSearchBooleanField() {
		Component ac = constructField("someBoolean", true);
		assertTrue(ac instanceof ComboBox);

		ComboBox<Boolean> cb = (ComboBox<Boolean>) ac;
		ListDataProvider<Boolean> ldp = (ListDataProvider<Boolean>) cb.getDataProvider();

		assertTrue(ldp.getItems().contains(Boolean.TRUE));
		assertTrue(ldp.getItems().contains(Boolean.FALSE));
	}

	@Test
	public void testNormalBooleanField() {
		Component ac = constructField("someBoolean", false);
		assertTrue(ac instanceof Checkbox);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityLookupField() {
		Component ac = constructField2("testEntity", false, false);
		assertTrue(ac instanceof EntityLookupField);

		EntityLookupField<Integer, TestEntity> lf = (EntityLookupField<Integer, TestEntity>) ac;
		assertNull(lf.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityLookupFieldWithFieldFilter() {
		Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntity", new EqualsPredicate<>("name", "Bob"));
		Component ac = constructField2("testEntity", fieldFilters);
		assertTrue(ac instanceof EntityLookupField);

		EntityLookupField<Integer, TestEntity> lf = (EntityLookupField<Integer, TestEntity>) ac;
		assertNotNull(lf.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityComboBox() {
		Component ac = constructField2("testEntityAlt", false, false);
		assertTrue(ac instanceof QuickAddEntityComboBox);

		QuickAddEntityComboBox<Integer, TestEntity> cb = (QuickAddEntityComboBox<Integer, TestEntity>) ac;
		assertNull(cb.getFilter());
	}

	/**
	 * Test that in search mode a token field is selected if multiple search is
	 * specified
	 */
	@Test
	public void testConstructEntityComboBoxMultipleSearch() {
		EntityModel<TestEntity2> em = factory.getModel("TestEntity2Multi", TestEntity2.class);
		FieldFactoryContext context = FieldFactoryContext.create()
				.setAttributeModel(em.getAttributeModel("testEntityAlt")).setSearch(true);
		assertThrows(OCSRuntimeException.class, () -> fieldFactory.constructField(context));
	}

	/**
	 * Test list select (and number of rows)
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testConstructListSingleSelect() {
		Component ac = constructField2("testEntityAlt2", false, false);
		assertTrue(ac instanceof QuickAddListSingleSelect);
		QuickAddListSingleSelect<Integer, TestEntity> ls = (QuickAddListSingleSelect<Integer, TestEntity>) ac;
		assertNull(ls.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructListSingleSelectWithFilter() {
		Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntityAlt2", new EqualsPredicate<>("name", "Bob"));
		Component ac = constructField2("testEntityAlt2", fieldFilters);
		assertTrue(ac instanceof QuickAddListSingleSelect);

		QuickAddListSingleSelect<Integer, TestEntity> ls = (QuickAddListSingleSelect<Integer, TestEntity>) ac;
		assertNotNull(ls.getFilter());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstructEntityComboBoxWithFieldFilter() {

		Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();
		fieldFilters.put("testEntityAlt", new EqualsPredicate<>("name", "Bob"));
		Component ac = constructField2("testEntityAlt", fieldFilters);
		assertTrue(ac instanceof QuickAddEntityComboBox);

		QuickAddEntityComboBox<Integer, TestEntity> cb = (QuickAddEntityComboBox<Integer, TestEntity>) ac;
		assertNotNull(cb.getFilter());
	}

	/**
	 * Test that in edit mode just a text field is constructed
	 */
	@Test
	public void testConstructSimpleTokenField2() {
		Component ac = constructField2("basicToken", false, false);
		assertTrue(ac instanceof TextField);
	}

	/**
	 * Token field select as default for detail relation
	 */
	@Test
	public void testDetailTokenFieldSelect() {
		Component ac = constructField("testEntities", false);
		assertTrue(ac instanceof QuickAddTokenSelect<?, ?>);
		QuickAddTokenSelect<?, ?> tf = (QuickAddTokenSelect<?, ?>) ac;
		assertNull(tf.getFilter());
	}

	/**
	 * Lookup field for detail collection
	 */
	@Test
	public void testDetailLookup() {
		Component ac = constructField("testEntities", "TestEntityLookup");
		assertTrue(ac instanceof EntityLookupField);

		EntityLookupField<?, ?> fl = (EntityLookupField<?, ?>) ac;
		assertNull(fl.getFilter());
		assertEquals("Test Entities", fl.getLabel());
		assertNull(fl.getAddButton());
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
