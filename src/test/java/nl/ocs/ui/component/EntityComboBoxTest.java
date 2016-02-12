package nl.ocs.ui.component;

import junitx.util.PrivateAccessor;
import nl.ocs.dao.SortOrder;
import nl.ocs.domain.TestEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.domain.model.impl.EntityModelFactoryImpl;
import nl.ocs.service.TestEntityService;
import nl.ocs.test.BaseMockitoTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.vaadin.data.util.filter.Compare;

public class EntityComboBoxTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		PrivateAccessor.setField(factory, "defaultPrecision", 2);
	}

	@Test
	public void test() {

	}

	@Test
	public void testAll() {
		AttributeModel am = factory.getModel(TestEntity.class).getAttributeModel("name");

		EntityComboBox<Integer, TestEntity> select = new EntityComboBox<>(
				factory.getModel(TestEntity.class), am, service);
		Assert.assertEquals(EntityComboBox.SelectMode.ALL, select.getSelectMode());
		Assert.assertEquals(am, select.getAttributeModel());

		Mockito.verify(service).findAll((SortOrder[]) null);
	}

	@Test
	public void testFixed() {
		EntityComboBox<Integer, TestEntity> select = new EntityComboBox<>(
				factory.getModel(TestEntity.class), null, Lists.newArrayList(new TestEntity()));
		Assert.assertEquals(EntityComboBox.SelectMode.FIXED, select.getSelectMode());

		Mockito.verifyZeroInteractions(service);
	}

	@Test
	public void testFilter() {
		EntityComboBox<Integer, TestEntity> select = new EntityComboBox<>(
				factory.getModel(TestEntity.class), null, service, new Compare.Equal("name", "Bob"));
		Assert.assertEquals(EntityComboBox.SelectMode.FILTERED, select.getSelectMode());

		Mockito.verify(service).find(Matchers.any(nl.ocs.filter.Filter.class),
				Matchers.any(nl.ocs.dao.SortOrder[].class));
	}
}
