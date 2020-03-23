package com.ocs.dynamo.ui.composite.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.service.TestEntity2Service;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class InMemoryTreeGridTest extends FrontendIntegrationTest {

    @Inject
    private TestEntityService testEntityService;

    @Inject
    private TestEntity2Service testEntity2Service;

    private TestEntity e1;

    private TestEntity e2;

    private TestEntity2 child1;

    private TestEntity2 child2;

    @BeforeEach
    public void setup() {
        e1 = new TestEntity("Bob", 11L);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Harry", 12L);
        e2 = testEntityService.save(e2);

        child1 = new TestEntity2();
        child1.setName("child1");
        child1.setTestEntity(e1);

        child1.setValueSum(9);

        child1 = testEntity2Service.save(child1);

        child2 = new TestEntity2();
        child2.setName("child2");
        child2.setTestEntity(e2);
        child2.setValueSum(11);

        child2 = testEntity2Service.save(child2);
    }

    @Test
    public void testBuildGrid() {

        final List<TestEntity> parents = Lists.newArrayList(e1, e2);
        final List<TestEntity2> children = Lists.newArrayList(child1, child2);

        InMemoryTreeGrid<TreeGridRow, Integer, TestEntity2, Integer, TestEntity> grid = new InMemoryTreeGrid<TreeGridRow, Integer, TestEntity2, Integer, TestEntity>() {

            private static final long serialVersionUID = -3834741496353866628L;

            private static final String VALUE = "value";

            private static final String VALUE_2 = "value2";

            private static final String VALUE_SUM = "valueSum";

            private static final String NAME = "name";

            private static final String LONG_VALUE = "longValue";

            @Override
            protected List<TestEntity2> getChildren(TestEntity parent) {
                List<TestEntity2> result = new ArrayList<>();
                for (TestEntity2 child : children) {
                    if (child.getTestEntity().equals(parent)) {
                        result.add(child);
                    }
                }
                return result;
            }

            @Override
            protected Class<?> getEditablePropertyClass(String columnName) {
                if (LONG_VALUE.equals(columnName)) {
                    return Long.class;
                }
                return Integer.class;
            }

            @Override
            protected String[] getSumColumns() {
                return new String[] { VALUE, VALUE_2, VALUE_SUM, LONG_VALUE };
            }

            @Override
            protected void addColumns() {
                addReadOnlyColumn(NAME, "Name", false);
                addReadOnlyColumn(VALUE, "Value", false);
                addReadOnlyColumn(VALUE_2, "Value2", false);
                addReadOnlyColumn(VALUE_SUM, "Value Sum", false);
                addReadOnlyColumn(LONG_VALUE, "Long Value", false);
            }

            @Override
            protected TreeGridRow createChildRow(TestEntity2 childEntity, TestEntity parentEntity) {
                TreeGridRow row = new TreeGridRow();

                row.setName(childEntity.getName());
                row.setValue(childEntity.getValue());
                row.setValue2(childEntity.getValue2());
                row.setValueSum(childEntity.getValueSum());
                row.setLongValue(4L);
                return row;
            }

            @Override
            protected TreeGridRow createParentRow(TestEntity entity) {
                TreeGridRow row = new TreeGridRow();
                row.setName(entity.getName());
                return row;
            }

            @Override
            protected List<TestEntity> getParentCollection() {
                return parents;
            }

            @Override
            protected Number extractSumCellValue(TreeGridRow t, int index, String columnName) {
                return (Number) ClassUtils.getFieldValue(t, columnName);
            }

            @Override
            protected void setSumCellValue(TreeGridRow t, int index, String columnName, BigDecimal value) {
                if (getEditablePropertyClass(columnName).equals(Integer.class)) {
                    ClassUtils.setFieldValue(t, columnName, value.intValue());
                } else {
                    ClassUtils.setFieldValue(t, columnName, value.longValue());
                }
            }
        };
        grid.build();

        assertEquals(5, grid.getColumns().size());

        TreeDataProvider<TreeGridRow> provider = (TreeDataProvider<TreeGridRow>) grid.getDataProvider();

        List<TreeGridRow> parentRows = provider.getTreeData().getRootItems();
        assertEquals(2, parentRows.size());

        TreeGridRow parent1 = parentRows.get(0);
        assertEquals(9, parent1.getValueSum().intValue());
        assertEquals(9, parent1.getValueSum().intValue());
        assertEquals(4L, parent1.getLongValue().longValue());

        TreeGridRow parent2 = parentRows.get(1);
        assertEquals(11, parent2.getValueSum().intValue());
        assertEquals(4L, parent2.getLongValue().longValue());

        List<TreeGridRow> childRows = provider.getTreeData().getChildren(parent1);
        assertEquals(1, childRows.size());

    }
}
