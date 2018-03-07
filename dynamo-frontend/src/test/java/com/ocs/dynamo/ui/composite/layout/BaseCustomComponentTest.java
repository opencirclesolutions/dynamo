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

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.persistence.OptimisticLockException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

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
import com.vaadin.ui.Label;

import junitx.util.PrivateAccessor;

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

	@Before
	public void setupBaseCustomComponentTest() throws NoSuchFieldException {
		System.setProperty(DynamoConstants.SP_DEFAULT_LOCALE, "de");
		MockUtil.mockMessageService(messageService);
		PrivateAccessor.setField(component, "messageService", messageService);
	}

	@Test
	public void test() {
		EntityModel<TestEntity> model = factory.getModel(TestEntity.class);

		DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(new Locale("de"));

		TestEntity e = new TestEntity("Kevin", 12L);
		e.setDiscount(BigDecimal.valueOf(12.34));
		e.setBirthDate(DateUtils.createDate("04052016"));
		e.setBirthWeek(DateUtils.createDate("04052016"));
		e.setSomeInt(1234);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 14);
		cal.set(Calendar.MINUTE, 25);
		cal.set(Calendar.SECOND, 37);
		e.setSomeTime(cal.getTime());

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

		Label label = (Label) component.constructLabel(e, model.getAttributeModel("name"));
		Assert.assertEquals("Kevin", label.getValue());

		// integer
		label = (Label) component.constructLabel(e, model.getAttributeModel("someInt"));
		Assert.assertEquals("1" + sym.getGroupingSeparator() + "234", label.getValue());

		// long
		label = (Label) component.constructLabel(e, model.getAttributeModel("age"));
		Assert.assertEquals("12", label.getValue());

		// BigDecimal
		label = (Label) component.constructLabel(e, model.getAttributeModel("discount"));
		Assert.assertEquals("12" + sym.getDecimalSeparator() + "34", label.getValue());

		// date
		label = (Label) component.constructLabel(e, model.getAttributeModel("birthDate"));
		Assert.assertEquals("04/05/2016", label.getValue());

		// week
		label = (Label) component.constructLabel(e, model.getAttributeModel("birthWeek"));
		Assert.assertEquals("2016-18", label.getValue());

		// time
		label = (Label) component.constructLabel(e, model.getAttributeModel("someTime"));
		Assert.assertEquals("14:25:37", label.getValue());

		// enum
		label = (Label) component.constructLabel(e, model.getAttributeModel("someEnum"));
		Assert.assertEquals("Value A", label.getValue());

		// entity collection
		label = (Label) component.constructLabel(e, model.getAttributeModel("testEntities"));
		Assert.assertEquals("Bob<br/> Stuart", label.getValue());

		// boolean
		label = (Label) component.constructLabel(e, model.getAttributeModel("someBoolean"));
		Assert.assertEquals("true", label.getValue());

		// boolean with overwritten value
		label = (Label) component.constructLabel(e, model.getAttributeModel("someBoolean2"));
		Assert.assertEquals("On", label.getValue());
	}

	@Test
	public void testHandleSaveException() {
		component.handleSaveException(new OCSValidationException("Some error"));
		component.handleSaveException(new OCSRuntimeException("Some error"));
		component.handleSaveException(new OptimisticLockException());
		component.handleSaveException(new RuntimeException());
	}

}
