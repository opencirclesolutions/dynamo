package com.ocs.dynamo.ui.container.hierarchical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.ui.container.EnergyUsage;
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalContainer.HierarchicalId;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class HierarchicalContainerTest {

	public class Week {
		int week;
		boolean hasChildren;

		public Week(int week, boolean hasChildren) {
			super();
			this.week = week;
			this.hasChildren = hasChildren;
		}

		/**
		 * @return the week
		 */
		public int getWeek() {
			return week;
		}

		/**
		 * @return the hasChildren
		 */
		public boolean hasChildren() {
			return hasChildren;
		}
	}

	HierarchicalContainer container;

	@Before
	public void setup() {
		// Define parent container
		Collection<Week> weeks = Arrays.asList(new Week(1, true), new Week(2, true),
		        new Week(3, true), new Week(4, true), new Week(5, false));
		BeanContainer<Integer, Week> parentContainer = new BeanContainer<>(Week.class);
		parentContainer.setBeanIdProperty("week");
		parentContainer.addAll(weeks);

		// Define child container
		Collection<EnergyUsage> usage = Lists.newArrayList(new EnergyUsage("ABC", 1, 1),
		        new EnergyUsage("DEF", 1, 1), new EnergyUsage("ABC", 2, 2),
		        new EnergyUsage("DEF", 2, 2), new EnergyUsage("GHI", 2, 2),
		        new EnergyUsage("ABC", 3, 3), new EnergyUsage("DEF", 3, 3),
		        new EnergyUsage("GHI", 3, 3), new EnergyUsage("ABC", 4, 4),
		        new EnergyUsage("DEF", 4, 4), new EnergyUsage("JKL", 4, 4));
		BeanContainer<String, EnergyUsage> childContainer = new BeanContainer<>(EnergyUsage.class);
		childContainer.setBeanIdProperty("id");
		childContainer.addAll(usage);

		// Define hierarchical container
		container = new HierarchicalContainer("week", "ean", "usage");
		container.addDefinition(parentContainer, 0, "week", null, "week", null, null);
		container.addDefinition(childContainer, 1, "id", "week", "week", "ean", "usage");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		// Get root
		List<?> roots = container.rootItemIds();
		assertNotNull(roots);
		assertEquals(5, roots.size());
		Object id = roots.get(1);
		assertTrue(id instanceof HierarchicalId);
		HierarchicalId rid = (HierarchicalId) id;
		assertEquals(0, rid.getLevel());
		assertEquals(2, rid.getItemId());

		// Get children of specific root
		List<?> childrenIds = container.getChildren(rid);
		assertNotNull(childrenIds);
		assertEquals(3, childrenIds.size());
		EnergyUsage eu = (EnergyUsage) ((BeanItem<EnergyUsage>) container
		        .getItem(childrenIds.get(1))).getBean();
		assertNotNull(eu);
		assertEquals("DEF", eu.getEan());

		// Get parent
		id = container.getParent(childrenIds.get(1));
		assertNotNull(id);
		assertTrue(id instanceof HierarchicalId);
		HierarchicalId pid = (HierarchicalId) id;
		assertEquals(0, pid.getLevel());
		assertEquals(2, pid.getItemId());
	}

}
