package nl.ocs.domain.model;

import javax.inject.Inject;

import nl.ocs.domain.TestEntity;
import nl.ocs.domain.TestEntity2;
import nl.ocs.domain.model.impl.ModelBasedFieldFactory;
import nl.ocs.service.MessageService;
import nl.ocs.test.BaseIntegrationTest;
import nl.ocs.ui.component.EntityComboBox;
import nl.ocs.ui.component.EntityComboBox.SelectMode;
import nl.ocs.ui.component.EntityListSelect;
import nl.ocs.ui.component.EntityLookupField;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Field;

public class ModelBasedFieldFactoryIntegrationTest extends BaseIntegrationTest {

	@Inject
	private EntityModelFactory entityModelFactory;

	@Inject
	private MessageService messageService;

	@Before
	public void setup() {
	}

	/**
	 * Test the creation of
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateLookupField() {

		EntityModel<TestEntity2> model = entityModelFactory.getModel("TestEntity2Lookup",
				TestEntity2.class);
		AttributeModel am = model.getAttributeModel("testEntity");

		ModelBasedFieldFactory<TestEntity2> factory = new ModelBasedFieldFactory<>(model,
				messageService, false, false);

		Field<?> field = factory.createField(am.getName());
		Assert.assertTrue(field instanceof EntityLookupField);

		EntityLookupField<Integer, TestEntity> f = (EntityLookupField<Integer, TestEntity>) field;
		Assert.assertEquals(new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING),
				f.getSortOrder());

	}

	/**
	 * Test the creation of a ListSelectComponent
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreatelistSelect() {

		EntityModel<TestEntity2> model = entityModelFactory.getModel("TestEntity2ListSelect",
				TestEntity2.class);
		AttributeModel am = model.getAttributeModel("testEntity");

		ModelBasedFieldFactory<TestEntity2> factory = new ModelBasedFieldFactory<>(model,
				messageService, false, false);

		Field<?> field = factory.createField(am.getName());
		Assert.assertTrue(field instanceof EntityListSelect);

		EntityListSelect<Integer, TestEntity> f = (EntityListSelect<Integer, TestEntity>) field;
		Assert.assertEquals(new com.vaadin.data.sort.SortOrder("name", SortDirection.ASCENDING),
				f.getSortOrder()[0]);
		Assert.assertEquals(nl.ocs.ui.component.EntityListSelect.SelectMode.FILTERED,
				f.getSelectMode());
		Assert.assertEquals(5, f.getRows());
	}

	/**
	 * Test the creation of a combo box
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateComboBox() {
		ModelBasedFieldFactory<TestEntity2> fieldFactory = ModelBasedFieldFactory.getInstance(
				entityModelFactory.getModel(TestEntity2.class), messageService);

		EntityModel<TestEntity2> model = entityModelFactory.getModel(TestEntity2.class);
		AttributeModel am = model.getAttributeModel("testEntity");

		Field<?> field = fieldFactory.constructComboBox(am.getNestedEntityModel(), am, null);
		Assert.assertTrue(field instanceof EntityComboBox);

		EntityComboBox<Integer, TestEntity> dc = (EntityComboBox<Integer, TestEntity>) field;
		Assert.assertEquals(SelectMode.FILTERED, dc.getSelectMode());

		SortOrder[] sortOrders = dc.getSortOrder();
		Assert.assertEquals(2, sortOrders.length);

		Assert.assertEquals("name", sortOrders[0].getPropertyId());
		Assert.assertEquals(SortDirection.ASCENDING, sortOrders[0].getDirection());

		Assert.assertEquals("age", sortOrders[1].getPropertyId());
		Assert.assertEquals(SortDirection.ASCENDING, sortOrders[1].getDirection());
	}
}
