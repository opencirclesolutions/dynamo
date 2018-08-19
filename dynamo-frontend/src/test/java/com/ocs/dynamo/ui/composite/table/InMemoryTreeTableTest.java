package com.ocs.dynamo.ui.composite.table;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.utils.ClassUtils;

public class InMemoryTreeTableTest extends BaseIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	@Inject
	private TestEntity2Service testEntity2Service;

	private TestEntity e1;

	private TestEntity e2;

	private TestEntity2 child1;

	private TestEntity2 child2;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2 = testEntityService.save(e2);

		child1 = new TestEntity2();
		child1.setName("child1");
		child1.setTestEntity(e1);
		child1 = testEntity2Service.save(child1);

		child2 = new TestEntity2();
		child2.setName("child2");
		child2.setTestEntity(e2);
		child2 = testEntity2Service.save(child2);
	}

	@Test
	public void test() {

		final List<TestEntity> parents = Lists.newArrayList(e1, e2);
		final List<TestEntity2> children = Lists.newArrayList(child1, child2);

		InMemoryTreeTable<Integer, TestEntity2, Integer, TestEntity> table = new InMemoryTreeTable<Integer, TestEntity2, Integer, TestEntity>(
		        true) {

			private static final long serialVersionUID = -3834741496353866628L;

			private static final String VALUE = "value";

			private static final String VALUE_2 = "value2";

			private static final String VALUE_SUM = "valueSum";

			private static final String NAME = "name";

			@Override
			protected boolean isRightAligned(String propertyId) {
				return false;
			}

			@Override
			protected boolean isEditable(String propertyId) {
				return "value".equals(propertyId);
			}

			@Override
			protected Number handleChange(String propertyId, String rowId, String parentRowId, String childKey,
			        String parentKey, Object newValue) {

				TestEntity2 found = null;
				for (TestEntity2 c : children) {
					if (c.getName().equals(childKey)) {
						found = c;
						break;
					}
				}

				if (found != null) {
					ClassUtils.setFieldValue(found, propertyId, toInt(newValue));
					int x1 = found.getValue() == null ? 0 : found.getValue();
					int x2 = found.getValue2() == null ? 0 : found.getValue2();

					found.setValueSum(x1 + x2);

					return toInt(newValue);
				}
				return null;
			}

			@Override
			protected List<TestEntity2> getRowCollection(TestEntity parent) {
				List<TestEntity2> result = new ArrayList<>();
				for (TestEntity2 child : children) {
					if (child.getTestEntity().equals(parent)) {
						result.add(child);
					}
				}
				return result;
			}

			@Override
			protected String getReportTitle() {
				return "Test Entity Report";
			}

			@Override
			protected String getPreviousColumnId(String columnId) {
				return null;
			}

			@Override
			protected List<TestEntity> getParentCollection() {
				return parents;
			}

			@Override
			protected String getKeyPropertyId() {
				return "name";
			}

			@Override
			protected Class<?> getEditablePropertyClass(String propertyId) {
				return Integer.class;
			}

			@Override
			protected String[] getColumnstoUpdate(String propertyId) {
				return new String[] { VALUE_SUM };
			}

			@Override
			protected void fillParentRow(Object[] row, TestEntity entity) {
				row[0] = entity.getName();
			}

			@Override
			protected void fillChildRow(Object[] row, TestEntity2 entity, TestEntity parentEntity) {
				row[0] = entity.getName();
				row[1] = entity.getValue();
				row[2] = entity.getValue2();
				row[3] = entity.getValueSum();
			}

			@Override
			protected void addContainerProperties() {
				addContainerProperty(NAME, String.class, null);
				addContainerProperty(VALUE, Integer.class, null);
				addContainerProperty(VALUE_2, Integer.class, null);
				addContainerProperty(VALUE_SUM, Integer.class, null);
			}

			@Override
			protected String[] getSumColumns() {
				return new String[] { VALUE, VALUE_2, VALUE_SUM };
			}
		};
		table.build();

		Assert.assertEquals(4, table.getItemIds().size());

		// try some changes
		table.handleChange("c0", "value", "4");
		table.handleChange("c0", "value2", "5");

		table.handleChange("c1", "value", "7");
		table.handleChange("c1", "value2", "8");

		// check sum and parent row
		Assert.assertEquals(9, table.getItem("c0").getItemProperty("valueSum").getValue());
		Assert.assertEquals(4, table.getItem("p0").getItemProperty("value").getValue());
		Assert.assertEquals(5, table.getItem("p0").getItemProperty("value2").getValue());

		Assert.assertEquals(15, table.getItem("c1").getItemProperty("valueSum").getValue());
		Assert.assertEquals(7, table.getItem("p1").getItemProperty("value").getValue());
		Assert.assertEquals(8, table.getItem("p1").getItemProperty("value2").getValue());

		Assert.assertEquals("11", table.getColumnFooter("value"));
		Assert.assertEquals("13", table.getColumnFooter("value2"));
		Assert.assertEquals("24", table.getColumnFooter("valueSum"));
	}
}
