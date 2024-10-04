package org.dynamoframework.utils;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;
import org.dynamoframework.configuration.DynamoConfigurationProperties;
import org.dynamoframework.configuration.DynamoPropertiesHolder;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.TestEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.domain.model.annotation.Model;
import org.dynamoframework.domain.model.impl.EntityModelFactoryImpl;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.test.BaseMockitoTest;
import org.dynamoframework.test.MockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import({EntityModelFactoryImpl.class, DynamoPropertiesHolder.class})
@EnableConfigurationProperties(value = DynamoConfigurationProperties.class)
public class FormatUtilsTest extends BaseMockitoTest {

	private static final Locale LOCALE = new Locale.Builder().setLanguage("nl").build();

	@Autowired
	private EntityModelFactory factory;// = new EntityModelFactoryImpl();

	@Mock
	private static MessageService messageService;

	@Mock
	private static ServiceLocator serviceLocator;

	@BeforeEach
	void beforeEach() {
		ReflectionTestUtils.setField(FormatUtils.class, "locator", serviceLocator);
		ReflectionTestUtils.setField(factory, "serviceLocator", serviceLocator);
		when(serviceLocator.getEntityModelFactory())
			.thenReturn(factory);
		when(serviceLocator.getMessageService())
			.thenReturn(messageService);
		MockUtil.mockMessageService(messageService);
	}

	@Test
	public void testFormatPropertyValue() {

		EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

		// simple string
		assertEquals("Bob", FormatUtils.formatPropertyValue(model.getAttributeModel("name"),
			"Bob", ", ", LOCALE));

		// boolean (without overrides)
		assertEquals("true", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean"), true, ", ", LOCALE));
		assertEquals("false", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean"), false, ", ", LOCALE));

		// boolean (with overrides)
		assertEquals("On", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean2"), true, ", ", LOCALE));
		assertEquals("Off", FormatUtils.formatPropertyValue(model.getAttributeModel("someBoolean2"), false, ", ", LOCALE));

		// enumeration
		assertEquals("A", FormatUtils.formatPropertyValue(model.getAttributeModel("someEnum"),
			TestEntity.TestEnum.A, ", ", LOCALE));

		// BigDecimal
		assertEquals("12,40", FormatUtils.formatPropertyValue(model.getAttributeModel("discount"), BigDecimal.valueOf(12.4), ", ", LOCALE));
		assertEquals("1.042,40", FormatUtils.formatPropertyValue(model.getAttributeModel("discount"), BigDecimal.valueOf(1042.4), ", ", LOCALE));
		assertEquals("1.042,40%", FormatUtils.formatPropertyValue(model.getAttributeModel("rate"), BigDecimal.valueOf(1042.4), ", ", LOCALE));

		// US formatting (reverse separators)
		assertEquals("1,000.40",
			FormatUtils.formatPropertyValue(model.getAttributeModel("discount"),
				BigDecimal.valueOf(1000.4), ", ", Locale.US));

		// date
		assertEquals("12/10/2015",
			FormatUtils.formatPropertyValue(model.getAttributeModel("birthDate"),
				DateUtils.createLocalDate("12102015"), ", ", LOCALE));

		// integer (with grouping)
		assertEquals("1.234", FormatUtils.formatPropertyValue(model.getAttributeModel("someInt"), 1234, ", ", LOCALE));

		// long
		assertEquals("1.234", FormatUtils.formatPropertyValue(model.getAttributeModel("age"),
			1234L, ", ", LOCALE));
	}

	@Test
	public void testFormatMasterEntity() {
		EntityModel<Entity2> model = factory.getModel(Entity2.class);
		AttributeModel at = model.getAttributeModel("entity1");
		assertNotNull(at);

		Entity1 e1 = new Entity1();
		e1.setId(1);
		e1.setName("some name");
		Entity2 e2 = new Entity2();
		e2.setId(2);
		e2.setSize(2);
		e2.setEntity1(e1);

		String result = FormatUtils.formatPropertyValue(model.getAttributeModel("entity1"), e1,
			", ", LOCALE);
		assertEquals("some name", result);
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
		e3.setEntities(Set.of(t1, t2, t3));

		String result = FormatUtils.formatEntityCollection(null, e3.getEntities(), ", ", LOCALE);
		assertTrue(result.contains("a1"));
		assertTrue(result.contains("a2"));
		assertTrue(result.contains("a3"));
	}

	@Model(displayProperty = "name")
	static class Entity1 extends AbstractEntity<Integer> {

		private static final long serialVersionUID = -706695912687382812L;

		private Integer id;

		private String name;

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

	@Getter
	@Setter
	static class Entity2 extends AbstractEntity<Integer> {

		private static final long serialVersionUID = -6048664928800386501L;

		private Integer id;

		private Integer size;

		private Entity1 entity1;

	}

	@Getter
	@Setter
	static class Entity3 extends AbstractEntity<Integer> {

		private static final long serialVersionUID = -6793879377561210713L;

		private Integer id;

		private Set<Entity1> entities = new HashSet<>();

	}

}
