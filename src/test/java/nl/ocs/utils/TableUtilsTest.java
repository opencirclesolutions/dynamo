package nl.ocs.utils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junitx.util.PrivateAccessor;
import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.TestEntity;
import nl.ocs.domain.TestEntity.TestEnum;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.domain.model.annotation.Model;
import nl.ocs.domain.model.impl.EntityModelFactoryImpl;
import nl.ocs.service.MessageService;
import nl.ocs.test.BaseMockitoTest;
import nl.ocs.test.MockitoSpringUtil;
import nl.ocs.ui.composite.table.TableUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.vaadin.ui.Table;

public class TableUtilsTest extends BaseMockitoTest {

	private static final Locale LOCALE = new Locale("nl");

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	@Mock
	private MessageService messageService;

	@Before
	public void setup() throws NoSuchFieldException {

		MockitoSpringUtil.mockMessageService(messageService);
		PrivateAccessor.setField(factory, "messageService", messageService);
		PrivateAccessor.setField(factory, "defaultPrecision", 2);
	}

	@Test
	public void testDefaultInit() {
		Table table = new Table();
		TableUtils.defaultInitialization(table);

		Assert.assertTrue(table.isImmediate());
		Assert.assertFalse(table.isEditable());
		Assert.assertFalse(table.isMultiSelect());
		Assert.assertTrue(table.isColumnReorderingAllowed());
		Assert.assertTrue(table.isColumnCollapsingAllowed());
		Assert.assertTrue(table.isSortEnabled());
	}

	@Test
	public void testFormatPropertyValue() {

		EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

		// simple string
		Assert.assertEquals("Bob", TableUtils.formatPropertyValue(factory, model, messageService,
				"name", "Bob", LOCALE));

		// boolean (without overrides)
		Assert.assertEquals("ocs.true", TableUtils.formatPropertyValue(factory, model,
				messageService, "someBoolean", true, LOCALE));
		Assert.assertEquals("ocs.false", TableUtils.formatPropertyValue(factory, model,
				messageService, "someBoolean", false, LOCALE));

		// boolean (with overrides)
		Assert.assertEquals("On", TableUtils.formatPropertyValue(factory, model, messageService,
				"someBoolean2", true, LOCALE));
		Assert.assertEquals("Off", TableUtils.formatPropertyValue(factory, model, messageService,
				"someBoolean2", false, LOCALE));

		// enumeration
		Assert.assertEquals("A", TableUtils.formatPropertyValue(factory, model, messageService,
				"someEnum", TestEnum.A, LOCALE));

		// BigDecimal
		Assert.assertEquals("12,40", TableUtils.formatPropertyValue(factory, model, messageService,
				"discount", BigDecimal.valueOf(12.4), LOCALE));
		Assert.assertEquals("1.042,40", TableUtils.formatPropertyValue(factory, model,
				messageService, "discount", BigDecimal.valueOf(1042.4), LOCALE));
		Assert.assertEquals(
				"1.042,40%",
				TableUtils.formatPropertyValue(factory, model, messageService, "rate",
						BigDecimal.valueOf(1042.4), LOCALE));

		// US formatting (reverse separators)
		Assert.assertEquals("1,000.40", TableUtils.formatPropertyValue(factory, model,
				messageService, "discount", BigDecimal.valueOf(1000.4), Locale.US));

		// date
		Assert.assertEquals("12/10/2015", TableUtils.formatPropertyValue(factory, model,
				messageService, "birthDate", DateUtils.createDate("12102015"), LOCALE));

		// date (as week)
		Assert.assertEquals("2015-42", TableUtils.formatPropertyValue(factory, model,
				messageService, "birthWeek", DateUtils.createDate("12102015"), LOCALE));

		// integer (with grouping)
		Assert.assertEquals("1.234", TableUtils.formatPropertyValue(factory, model, messageService,
				"someInt", 1234, LOCALE));

		// long
		Assert.assertEquals("1.234", TableUtils.formatPropertyValue(factory, model, messageService,
				"age", 1234L, LOCALE));
	}

	@Test
	public void testFormatMasterEntity() {
		EntityModel<Entity2> model = factory.getModel(Entity2.class);
		AttributeModel at = model.getAttributeModel("entity1");
		Assert.assertNotNull(at);

		Entity1 e1 = new Entity1();
		e1.setId(1);
		e1.setName("some name");
		Entity2 e2 = new Entity2();
		e2.setId(2);
		e2.setSize(2);
		e2.setEntity1(e1);

		String result = TableUtils.formatPropertyValue(factory, model, messageService, "entity1",
				e1, LOCALE);
		Assert.assertEquals("some name", result);
	}

	@Test
	public void testFormatEntityCollection() {

		Entity1 t1 = new Entity1();
		t1.setId(1);
		t1.setName("a1");

		Entity1 t2 = new Entity1();
		t2.setId(2);
		t2.setName("a2");

		Entity1 t3 = new Entity1();
		t3.setId(3);
		t3.setName("a3");

		Entity3 e3 = new Entity3();
		e3.setEntities(Sets.newHashSet(t1, t2, t3));

		String result = TableUtils.formatEntityCollection(factory, e3.getEntities());
		Assert.assertEquals("a1, a2, a3", result);
	}

	@Model(displayProperty = "name")
	class Entity1 extends AbstractEntity<Integer> {
		private static final long serialVersionUID = 6641700854398041107L;
		Integer id;
		String name;

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	class Entity2 extends AbstractEntity<Integer> {
		private static final long serialVersionUID = -422147335118151599L;
		Integer id;
		Integer size;
		Entity1 entity1;

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public void setId(Integer id) {
			this.id = id;
		}

		public Integer getSize() {
			return size;
		}

		public void setSize(Integer size) {
			this.size = size;
		}

		public Entity1 getEntity1() {
			return entity1;
		}

		public void setEntity1(Entity1 entity1) {
			this.entity1 = entity1;
		}
	}

	class Entity3 extends AbstractEntity<Integer> {

		private static final long serialVersionUID = -943613104435087634L;

		private Integer id;

		private Set<Entity1> entities = new HashSet<>();

		@Override
		public Integer getId() {
			return id;
		}

		@Override
		public void setId(Integer id) {
			this.id = id;

		}

		public Set<Entity1> getEntities() {
			return entities;
		}

		public void setEntities(Set<Entity1> entities) {
			this.entities = entities;
		}
	}
}
