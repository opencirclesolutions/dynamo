package nl.ocs.ui.component;

import junitx.util.PrivateAccessor;
import nl.ocs.domain.TestEntity;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.domain.model.impl.EntityModelFactoryImpl;
import nl.ocs.service.TestEntityService;
import nl.ocs.test.BaseMockitoTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Component;

public class EntityLookupFieldTest extends BaseMockitoTest {

	@Mock
	private TestEntityService service;

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		PrivateAccessor.setField(factory, "defaultPrecision", 2);
	}

	@Test
	public void test() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
				factory.getModel(TestEntity.class), null, null, new SortOrder("name",
						SortDirection.ASCENDING));
		field.initContent();

		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
		Assert.assertEquals(TestEntity.class, field.getType());

		Component comp = field.iterator().next();
		Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

		try {
			field.getSelectButton().click();
		} catch (com.vaadin.event.ListenerMethod.MethodException ex) {
			// expected since there is no UI
			Assert.assertTrue(ex.getCause() instanceof NullPointerException);
		}

		field.setEnabled(false);
		Assert.assertFalse(field.getSelectButton().isEnabled());
		Assert.assertFalse(field.getClearButton().isEnabled());
	}

	@Test
	public void testPageLength() {
		EntityLookupField<Integer, TestEntity> field = new EntityLookupField<>(service,
				factory.getModel(TestEntity.class), null, null, new SortOrder("name",
						SortDirection.ASCENDING));
		field.setPageLength(10);
		field.initContent();

		Assert.assertEquals(new SortOrder("name", SortDirection.ASCENDING), field.getSortOrder());
		Assert.assertEquals(TestEntity.class, field.getType());

		Component comp = field.iterator().next();
		Assert.assertTrue(comp instanceof DefaultHorizontalLayout);

		try {
			field.getSelectButton().click();
		} catch (com.vaadin.event.ListenerMethod.MethodException ex) {
			// expected since there is no UI
			Assert.assertTrue(ex.getCause() instanceof NullPointerException);
		}

	}
}
