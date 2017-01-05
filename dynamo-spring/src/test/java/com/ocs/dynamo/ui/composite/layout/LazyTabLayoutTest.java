package com.ocs.dynamo.ui.composite.layout;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class LazyTabLayoutTest extends BaseIntegrationTest {

	@Inject
	private TestEntityService testEntityService;

	private TestEntity e1;

	private TestEntity e2;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2 = testEntityService.save(e2);
	}

	@Test
	public void test() {
		LazyTabLayout<Integer, TestEntity> layout = new LazyTabLayout<Integer, TestEntity>(e1) {

			private static final long serialVersionUID = 1L;

			@Override
			protected Component initTab(int index) {
				switch (index) {
				case 0:
					return new HorizontalLayout();
				case 1:
					return new HorizontalLayout();
				default:
					return null;
				}
			}

			@Override
			protected String[] getTabCaptions() {
				return new String[] { "tab1", "tab2" };
			}

			@Override
			protected String getTabDescription(int index) {
				switch (index) {
				case 0:
					return "tab1 description";
				case 1:
					return "tab2 description";
				default:
					return null;
				}
			}

			@Override
			protected String createTitle() {
				return "Test Tab Layout";
			}
		};
		layout.build();

		Assert.assertTrue(layout.getTab(0).getComponent() instanceof VerticalLayout);
		VerticalLayout vl = (VerticalLayout) layout.getTab(0).getComponent();
		Assert.assertEquals(1, vl.getComponentCount());
		Assert.assertEquals("tab1", layout.getTab(0).getCaption());
		Assert.assertEquals("tab1 description", layout.getTab(0).getDescription());

		// second tab has not been created yet
		VerticalLayout vl2 = (VerticalLayout) layout.getTab(1).getComponent();
		Assert.assertEquals(0, vl2.getComponentCount());

		// select the second tab, this will lazily create it
		layout.selectTab(1);
		vl2 = (VerticalLayout) layout.getTab(1).getComponent();
		Assert.assertEquals("tab2", layout.getTab(1).getCaption());
		Assert.assertEquals("tab2 description", layout.getTab(1).getDescription());
		Assert.assertEquals(1, vl2.getComponentCount());

		// select the tab again (triggers a reload)
		layout.selectTab(1);
		vl2 = (VerticalLayout) layout.getTab(1).getComponent();
		Assert.assertEquals(1, vl2.getComponentCount());

	}
}
