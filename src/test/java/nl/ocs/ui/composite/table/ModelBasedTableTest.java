package nl.ocs.ui.composite.table;

import java.math.BigDecimal;

import junitx.util.PrivateAccessor;
import nl.ocs.domain.TestEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.domain.model.impl.EntityModelFactoryImpl;
import nl.ocs.service.MessageService;
import nl.ocs.service.TestEntityService;
import nl.ocs.test.BaseMockitoTest;
import nl.ocs.test.MockitoSpringUtil;
import nl.ocs.ui.composite.table.FixedTableWrapper;
import nl.ocs.ui.composite.table.ModelBasedTable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;

public class ModelBasedTableTest extends BaseMockitoTest {

	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService;

	@Mock
	private TestEntityService service;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Mockito.when(service.getEntityClass()).thenReturn(TestEntity.class);

		MockitoSpringUtil.mockMessageService(messageService);
		PrivateAccessor.setField(entityModelFactory, "messageService", messageService);
		PrivateAccessor.setField(entityModelFactory, "defaultPrecision", 2);
	}

	@Test
	public void testBeanItemContainer() {
		BeanItemContainer<Person> container = new BeanItemContainer<>(Person.class);
		EntityModel<Person> model = entityModelFactory.getModel(Person.class);

		Person person = new Person(1, "Bob", 50, BigDecimal.valueOf(76.4), BigDecimal.valueOf(44.4));
		container.addItem(person);

		ModelBasedTable<Integer, Person> table = new ModelBasedTable<>(container, model,
				entityModelFactory, messageService);

		Assert.assertEquals("Persons", table.getCaption());
		Assert.assertEquals("Person", table.getDescription());
		Assert.assertEquals(4, table.getVisibleColumns().length);

		// numeric column aligned to the right
		Assert.assertEquals(Table.Align.RIGHT, table.getColumnAlignment("age"));

		String result = table.formatPropertyValue(person, "age", table.getItem(person)
				.getItemProperty("age"));
		Assert.assertEquals("50", result);
	}

	@Test
	public void testFixedTableWrapper() {
		TestEntity entity = new TestEntity();

		EntityModel<TestEntity> model = entityModelFactory.getModel(TestEntity.class);
		FixedTableWrapper<Integer, TestEntity> wrapper = new FixedTableWrapper<Integer, TestEntity>(
				service, model, Lists.newArrayList(entity), null);
		wrapper.build();

		Table table = wrapper.getTable();
		Assert.assertNotNull(table);

		Assert.assertEquals(1, wrapper.getTable().getContainerDataSource().size());
	}

}
