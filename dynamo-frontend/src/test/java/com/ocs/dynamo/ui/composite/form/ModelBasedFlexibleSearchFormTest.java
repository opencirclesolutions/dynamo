package com.ocs.dynamo.ui.composite.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.FlexibleFilterDefinition;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.container.QueryType;

public class ModelBasedFlexibleSearchFormTest extends BaseIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private EntityModelFactory entityModelFactory;

	private TestEntity e1;

	private TestEntity e2;

	private ServiceResultsTableWrapper<Integer, TestEntity> wrapper;

	private ModelBasedFlexibleSearchForm<Integer, TestEntity> form;

	private EntityModel<TestEntity> em;

	private AttributeModel am;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1.setRate(BigDecimal.valueOf(4));
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2.setRate(BigDecimal.valueOf(3));
		e2 = testEntityService.save(e2);

		entityModelFactory.getModel(TestEntity.class);

		em = entityModelFactory.getModel(TestEntity.class);
		am = em.getAttributeModel("name");
		build(em);
	}

	@Test
	public void testSearchAndClear() {
		EntityModel<TestEntity> em = entityModelFactory.getModel(TestEntity.class);
		AttributeModel am = em.getAttributeModel("name");

		// unfiltered search, returns two
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());

		// "equals" search
		form.addFilter(am, FlexibleFilterType.EQUALS, "Harry", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());

		// clear the search filters
		form.getClearButton().click();
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testStringContains() {
		form.addFilter(am, FlexibleFilterType.CONTAINS, "a", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testStringNotEquals() {
		form.addFilter(am, FlexibleFilterType.NOT_EQUAL, "Bob", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testStringNotContains() {
		form.addFilter(am, FlexibleFilterType.NOT_CONTAINS, "x", null);
		form.search();
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testStringStartsWith() {
		form.addFilter(am, FlexibleFilterType.STARTS_WITH, "a", null);
		form.search();
		Assert.assertEquals(0, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testNotStringStartsWith() {
		form.addFilter(am, FlexibleFilterType.NOT_STARTS_WITH, "a", null);
		form.search();
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testNumeric() {
		am = em.getAttributeModel("age");
		form.addFilter(am, FlexibleFilterType.EQUALS, "11", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());

		// replace by a "between" filter
		form.addFilter(am, FlexibleFilterType.BETWEEN, "11", "14");
		form.search();
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());

		// replace by a "greater than" filter
		form.addFilter(am, FlexibleFilterType.GREATER_THAN, "12", null);
		form.search();
		Assert.assertEquals(0, wrapper.getTable().getContainerDataSource().size());

		// replace by a "less than" filter
		form.addFilter(am, FlexibleFilterType.LESS_THAN, "12", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());

		// replace by a "less than or equal" filter
		form.addFilter(am, FlexibleFilterType.LESS_OR_EQUAL, "12", null);
		form.search();
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());

		// replace by a "greater or equal" filter
		form.addFilter(am, FlexibleFilterType.GREATER_OR_EQUAL, "13", null);
		form.search();
		Assert.assertEquals(0, wrapper.getTable().getContainerDataSource().size());

	}

	@Test
	public void testEnum() {
		am = em.getAttributeModel("someEnum");
		form.addFilter(am, FlexibleFilterType.EQUALS, TestEnum.A, null);
		form.search();
		Assert.assertEquals(0, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testBoolean() {
		am = em.getAttributeModel("someBoolean");
		form.addFilter(am, FlexibleFilterType.EQUALS, true, null);
		form.search();
		Assert.assertEquals(0, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testBigDecimal() {
		am = em.getAttributeModel("rate");
		form.addFilter(am, FlexibleFilterType.EQUALS, "4", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());

		// replace by a "between" filter
		form.addFilter(am, FlexibleFilterType.BETWEEN, "1", "5");
		form.search();
		Assert.assertEquals(2, wrapper.getTable().getContainerDataSource().size());

		// replace by a "greater than" filter
		form.addFilter(am, FlexibleFilterType.GREATER_THAN, "3", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());

		// replace by a "less than" filter
		form.addFilter(am, FlexibleFilterType.LESS_THAN, "2", null);
		form.search();
		Assert.assertEquals(0, wrapper.getTable().getContainerDataSource().size());

		// replace by a "less than or equal" filter
		form.addFilter(am, FlexibleFilterType.LESS_OR_EQUAL, "3", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());

		// replace by a "greater or equal" filter
		form.addFilter(am, FlexibleFilterType.GREATER_OR_EQUAL, "4", null);
		form.search();
		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());
	}

	@Test
	public void testRestoreDefinitions() {

		List<FlexibleFilterDefinition> definitions = new ArrayList<>();

		FlexibleFilterDefinition def1 = new FlexibleFilterDefinition();
		def1.setAttributeModel(em.getAttributeModel("rate"));
		def1.setFlexibleFilterType(FlexibleFilterType.EQUALS);
		def1.setValue(BigDecimal.valueOf(45));
		definitions.add(def1);

		FlexibleFilterDefinition def2 = new FlexibleFilterDefinition();
		def2.setAttributeModel(em.getAttributeModel("age"));
		def2.setFlexibleFilterType(FlexibleFilterType.BETWEEN);
		def2.setValue(77L);
		def2.setValueTo(80L);
		definitions.add(def2);

		form.restoreFilterDefinitions(definitions);

		Assert.assertTrue(form.hasFilter(em.getAttributeModel("rate")));
		Assert.assertTrue(form.hasFilter(em.getAttributeModel("age")));
	}

	private void build(EntityModel<TestEntity> em) {
		wrapper = new ServiceResultsTableWrapper<>(testEntityService, em, QueryType.ID_BASED, null, null, false);
		wrapper.build();

		form = new ModelBasedFlexibleSearchForm<>(wrapper, em, new FormOptions());
		form.build();
	}
}
