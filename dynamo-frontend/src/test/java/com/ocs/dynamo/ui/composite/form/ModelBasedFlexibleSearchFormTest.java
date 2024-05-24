package com.ocs.dynamo.ui.composite.form;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.FlexibleFilterDefinition;
import com.ocs.dynamo.filter.FlexibleFilterType;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.function.SerializablePredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelBasedFlexibleSearchFormTest extends FrontendIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private EntityModelFactory entityModelFactory;

	private ServiceBasedGridWrapper<Integer, TestEntity> wrapper;

	private ModelBasedFlexibleSearchForm<Integer, TestEntity> form;

	private EntityModel<TestEntity> em;

	private AttributeModel am;

	@BeforeEach
	public void setup() {
		MockVaadin.setup();
		TestEntity entity1 = new TestEntity("Bob", 11L);
		entity1.setRate(BigDecimal.valueOf(4));
		testEntityService.save(entity1);

		TestEntity entity2 = new TestEntity("Harry", 12L);
		entity2.setRate(BigDecimal.valueOf(3));
		testEntityService.save(entity2);

		entityModelFactory.getModel(TestEntity.class);

		em = entityModelFactory.getModel(TestEntity.class);
		am = em.getAttributeModel("name");
		build(em);
	}

	private void search() {
		form.search();
		wrapper.forceSearch();
	}

	private void clear() {
		form.getClearButton().click();
	}

	@Test
	public void testSearchAndClear() {
		EntityModel<TestEntity> em = entityModelFactory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");

		search();

		// unfiltered search, returns two items
		assertEquals(2, wrapper.getDataProviderSize());

		// "equals" search
		form.addFilter(am, FlexibleFilterType.EQUALS, "Harry", null);
		search();

		assertEquals(1, wrapper.getDataProviderSize());

		// clear the search filters (verify that 2 items are once again returned)
		clear();
		wrapper.forceSearch();
		assertEquals(2, wrapper.getDataProviderSize());
	}

	@Test
	public void testStringContains() {
		form.addFilter(am, FlexibleFilterType.CONTAINS, "a", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());
	}

	@Test
	public void testStringNotEquals() {
		form.addFilter(am, FlexibleFilterType.NOT_EQUAL, "Bob", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());
	}

	@Test
	public void testStringNotContains() {
		form.addFilter(am, FlexibleFilterType.NOT_CONTAINS, "x", null);
		search();
		assertEquals(2, wrapper.getDataProviderSize());
	}

	@Test
	public void testStringStartsWith() {
		form.addFilter(am, FlexibleFilterType.STARTS_WITH, "a", null);
		search();
		assertEquals(0, wrapper.getDataProviderSize());
	}

	@Test
	public void testNotStringStartsWith() {
		form.addFilter(am, FlexibleFilterType.NOT_STARTS_WITH, "a", null);
		search();
		assertEquals(2, wrapper.getDataProviderSize());
	}

	@Test
	public void testNumeric() {
		am = em.getAttributeModel("age");
		form.addFilter(am, FlexibleFilterType.EQUALS, "11", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());

		// replace by a "between" filter
		form.addFilter(am, FlexibleFilterType.BETWEEN, "11", "14");
		search();
		assertEquals(2, wrapper.getDataProviderSize());

		// replace by a "greater than" filter
		form.addFilter(am, FlexibleFilterType.GREATER_THAN, "12", null);
		search();
		assertEquals(0, wrapper.getDataProviderSize());

		// replace by a "less than" filter
		form.addFilter(am, FlexibleFilterType.LESS_THAN, "12", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());

		// replace by a "less than or equal" filter
		form.addFilter(am, FlexibleFilterType.LESS_OR_EQUAL, "12", null);
		search();
		assertEquals(2, wrapper.getDataProviderSize());

		// replace by a "greater or equal" filter
		form.addFilter(am, FlexibleFilterType.GREATER_OR_EQUAL, "13", null);
		search();
		assertEquals(0, wrapper.getDataProviderSize());

	}

	@Test
	public void testEnum() {
		am = em.getAttributeModel("someEnum");
		form.addFilter(am, FlexibleFilterType.EQUALS, TestEnum.A, null);
		search();
		assertEquals(0, wrapper.getDataProviderSize());
	}

	@Test
	public void testBoolean() {
		am = em.getAttributeModel("someBoolean");
		form.addFilter(am, FlexibleFilterType.EQUALS, true, null);
		search();
		assertEquals(0, wrapper.getDataProviderSize());
	}

	@Test
	public void testBigDecimal() {
		am = em.getAttributeModel("rate");
		form.addFilter(am, FlexibleFilterType.EQUALS, "4", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());

		// replace by a "between" filter
		form.addFilter(am, FlexibleFilterType.BETWEEN, "1", "5");
		search();
		assertEquals(2, wrapper.getDataProviderSize());

		// replace by a "greater than" filter
		form.addFilter(am, FlexibleFilterType.GREATER_THAN, "3", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());

		// replace by a "less than" filter
		form.addFilter(am, FlexibleFilterType.LESS_THAN, "2", null);
		search();
		assertEquals(0, wrapper.getDataProviderSize());

		// replace by a "less than or equal" filter
		form.addFilter(am, FlexibleFilterType.LESS_OR_EQUAL, "3", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());

		// replace by a "greater or equal" filter
		form.addFilter(am, FlexibleFilterType.GREATER_OR_EQUAL, "4", null);
		search();
		assertEquals(1, wrapper.getDataProviderSize());
	}

	@Test
	public void testRestoreDefinitions() {

		List<FlexibleFilterDefinition> definitions = new ArrayList<>();

		FlexibleFilterDefinition def1 = FlexibleFilterDefinition.builder().attributeModel(em.getAttributeModel("rate"))
				.flexibleFilterType(FlexibleFilterType.EQUALS).value(BigDecimal.valueOf(45)).build();
		definitions.add(def1);

		FlexibleFilterDefinition def2 = FlexibleFilterDefinition.builder().attributeModel(em.getAttributeModel("age"))
				.flexibleFilterType(FlexibleFilterType.BETWEEN).value(77L).valueTo(80L).build();
		definitions.add(def2);

		form.restoreFilterDefinitions(definitions);

		assertTrue(form.hasFilter(em.getAttributeModel("rate")));
		assertTrue(form.hasFilter(em.getAttributeModel("age")));
	}

	private void build(EntityModel<TestEntity> em) {
		wrapper = new ServiceBasedGridWrapper<>(testEntityService, em, QueryType.ID_BASED, new FormOptions(),
				ComponentContext.<Integer, TestEntity>builder().build(), null,
				new HashMap<String, SerializablePredicate<?>>(), null, true);
		wrapper.build();

		form = new ModelBasedFlexibleSearchForm<>(wrapper, em, new FormOptions());
		form.build();
	}
}
