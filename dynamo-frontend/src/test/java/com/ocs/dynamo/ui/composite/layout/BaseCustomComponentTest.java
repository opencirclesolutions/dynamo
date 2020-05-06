/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.composite.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.util.Locale;

import javax.persistence.OptimisticLockException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.flow.component.html.Span;

public class BaseCustomComponentTest extends BaseMockitoTest {

	private EntityModelFactory factory = new EntityModelFactoryImpl();

	private BaseCustomComponent component = new BaseCustomComponent() {

		private static final long serialVersionUID = -714656253533978108L;

		@Override
		public void build() {
		}
	};

	@Mock
	private MessageService messageService;

	@BeforeEach
	public void setupBaseCustomComponentTest() throws NoSuchFieldException {
		System.setProperty(DynamoConstants.SP_DEFAULT_LOCALE, "de");
		MockUtil.mockMessageService(messageService);
		ReflectionTestUtils.setField(component, "messageService", messageService);
	}

	@Test
	public void test() {
		EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(new Locale("nl"));

		TestEntity e = new TestEntity("Kevin", 12L);
		e.setDiscount(BigDecimal.valueOf(12.34));
		e.setBirthDate(DateUtils.createLocalDate("04052016"));
		e.setBirthWeek(DateUtils.createLocalDate("04052016"));
		e.setSomeInt(1234);

		e.setSomeTime(LocalTime.of(14, 25, 37));

		e.setSomeEnum(TestEnum.A);
		e.setSomeBoolean(Boolean.TRUE);
		e.setSomeBoolean2(Boolean.TRUE);

		TestEntity2 te2 = new TestEntity2();
		te2.setName("Bob");
		te2.setId(2);
		e.addTestEntity2(te2);

		TestEntity2 te3 = new TestEntity2();
		te3.setName("Stuart");
		te3.setId(3);
		e.addTestEntity2(te3);

		Span text = (Span) component.constructLabel(e, model.getAttributeModel("name"));
		assertEquals("Kevin", text.getText());

		// integer
		text = (Span) component.constructLabel(e, model.getAttributeModel("someInt"));
		assertEquals("1" + sym.getGroupingSeparator() + "234", text.getText());

		// long
		text = (Span) component.constructLabel(e, model.getAttributeModel("age"));
		assertEquals("12", text.getText());

		// BigDecimal
		text = (Span) component.constructLabel(e, model.getAttributeModel("discount"));
		assertEquals("12" + sym.getDecimalSeparator() + "34", text.getText());

		// date
		text = (Span) component.constructLabel(e, model.getAttributeModel("birthDate"));
		assertEquals("04/05/2016", text.getText());

		// week
		text = (Span) component.constructLabel(e, model.getAttributeModel("birthWeek"));
		assertEquals("2016-18", text.getText());

		// time
		text = (Span) component.constructLabel(e, model.getAttributeModel("someTime"));
		assertEquals("14:25:37", text.getText());

		// enum
		text = (Span) component.constructLabel(e, model.getAttributeModel("someEnum"));
		assertEquals("Value A", text.getText());

		// entity collection
		text = (Span) component.constructLabel(e, model.getAttributeModel("testEntities"));
		assertEquals("Bob, Stuart", text.getText());

		// boolean
		text = (Span) component.constructLabel(e, model.getAttributeModel("someBoolean"));
		assertEquals("true", text.getText());

		// boolean with overwritten value
		text = (Span) component.constructLabel(e, model.getAttributeModel("someBoolean2"));
		assertEquals("On", text.getText());
	}

	@Test
	public void testHandleSaveException() {
		component.handleSaveException(new OCSValidationException("Some error"));
		component.handleSaveException(new OCSRuntimeException("Some error"));
		component.handleSaveException(new OptimisticLockException());
		component.handleSaveException(new RuntimeException());
	}

}
