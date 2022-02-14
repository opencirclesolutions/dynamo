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
package com.ocs.dynamo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.impl.BooleanCheckboxCreator;
import com.ocs.dynamo.domain.model.impl.BooleanComboboxCreator;
import com.ocs.dynamo.domain.model.impl.ComboBoxComponentCreator;
import com.ocs.dynamo.domain.model.impl.DatePickerComponentCreator;
import com.ocs.dynamo.domain.model.impl.DateTimePickerComponentCreator;
import com.ocs.dynamo.domain.model.impl.ElementCollectionComponentCreator;
import com.ocs.dynamo.domain.model.impl.EmailFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.EnumFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;
import com.ocs.dynamo.domain.model.impl.ListBoxComponentCreator;
import com.ocs.dynamo.domain.model.impl.LookupFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.SimpleTokenFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.TextAreaComponentCreator;
import com.ocs.dynamo.domain.model.impl.TextFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.TimePickerComponentCreator;
import com.ocs.dynamo.domain.model.impl.TokenFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.UploadComponentCreator;
import com.ocs.dynamo.domain.model.impl.UrlFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.WeekFieldComponentCreator;
import com.ocs.dynamo.domain.model.impl.ZonedDateTimePickerComponentCreator;
import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.service.impl.DefaultUserDetailsServiceImpl;
import com.ocs.dynamo.ui.auth.AuthorizationServiceInitListener;
import com.ocs.dynamo.ui.auth.PermissionChecker;
import com.ocs.dynamo.ui.auth.impl.DefaultPermissionCheckerImpl;
import com.ocs.dynamo.ui.menu.MenuService;

/**
 * Spring Boot auto configuration for the UI module
 * 
 * @author Bas Rutten
 *
 */
@Configuration
public class DynamoFrontendAutoConfigure {

	@Bean
	@Order(1000)
	@ConditionalOnMissingBean(ElementCollectionComponentCreator.class)
	public ElementCollectionComponentCreator elementCollectionComponentCreator() {
		return new ElementCollectionComponentCreator();
	}

	@Bean
	@Order(1010)
	@SuppressWarnings("rawtypes")
	@ConditionalOnMissingBean(SimpleTokenFieldComponentCreator.class)
	public SimpleTokenFieldComponentCreator simpleTokenFieldCreator() {
		return new SimpleTokenFieldComponentCreator();
	}

	@Bean
	@Order(1020)
	@SuppressWarnings("rawtypes")
	@ConditionalOnMissingBean(TokenFieldComponentCreator.class)
	public TokenFieldComponentCreator tokenFieldComponentCreator() {
		return new TokenFieldComponentCreator<>();
	}

	@Bean
	@Order(1030)
	@SuppressWarnings("rawtypes")
	@ConditionalOnMissingBean(LookupFieldComponentCreator.class)
	public LookupFieldComponentCreator lookupFieldComponentCreator() {
		return new LookupFieldComponentCreator<>();
	}

	@Bean
	@Order(1040)
	@SuppressWarnings("rawtypes")
	@ConditionalOnMissingBean(ComboBoxComponentCreator.class)
	public ComboBoxComponentCreator comboBoxComponentCreator() {
		return new ComboBoxComponentCreator();
	}

	@Bean
	@Order(1050)
	@SuppressWarnings("rawtypes")
	@ConditionalOnMissingBean(ListBoxComponentCreator.class)
	public ListBoxComponentCreator listBoxComponentCreator() {
		return new ListBoxComponentCreator();
	}

	@Bean
	@Order(1060)
	@ConditionalOnMissingBean(TextAreaComponentCreator.class)
	public TextAreaComponentCreator textAreaComponentCreator() {
		return new TextAreaComponentCreator();
	}

	@Bean
	@Order(1065)
	@ConditionalOnMissingBean(EmailFieldComponentCreator.class)
	public EmailFieldComponentCreator emailFieldComponentCreator() {
		return new EmailFieldComponentCreator();
	}
	
	@Bean
	@Order(1070)
	@ConditionalOnMissingBean(TextFieldComponentCreator.class)
	public TextFieldComponentCreator textFieldComponentCreator() {
		return new TextFieldComponentCreator();
	}

	@Bean
	@Order(1080)
	@ConditionalOnMissingBean(EnumFieldComponentCreator.class)
	public EnumFieldComponentCreator enumFieldComponentCreator() {
		return new EnumFieldComponentCreator();
	}

	@Bean
	@Order(1090)
	@ConditionalOnMissingBean(BooleanCheckboxCreator.class)
	public BooleanCheckboxCreator booleanCheckboxCreator() {
		return new BooleanCheckboxCreator();
	}

	@Bean
	@Order(1100)
	@ConditionalOnMissingBean(BooleanComboboxCreator.class)
	public BooleanComboboxCreator BooleanComboboxCreator() {
		return new BooleanComboboxCreator();
	}

	@Bean
	@Order(1110)
	@ConditionalOnMissingBean(DatePickerComponentCreator.class)
	public DatePickerComponentCreator DatePickerComponentCreator() {
		return new DatePickerComponentCreator();
	}

	@Bean
	@Order(1120)
	@ConditionalOnMissingBean(DateTimePickerComponentCreator.class)
	public DateTimePickerComponentCreator dateTimePickerComponentCreator() {
		return new DateTimePickerComponentCreator();
	}

	@Bean
	@Order(1130)
	@ConditionalOnMissingBean(ZonedDateTimePickerComponentCreator.class)
	public ZonedDateTimePickerComponentCreator zonedDateTimePickerComponentCreator() {
		return new ZonedDateTimePickerComponentCreator();
	}

	@Bean
	@Order(1140)
	@ConditionalOnMissingBean(TimePickerComponentCreator.class)
	public TimePickerComponentCreator timePickerComponentCreator() {
		return new TimePickerComponentCreator();
	}

	@Bean
	@Order(1150)
	@ConditionalOnMissingBean(UrlFieldComponentCreator.class)
	public UrlFieldComponentCreator urlFieldComponentCreator() {
		return new UrlFieldComponentCreator();
	}

	@Bean
	@Order(1160)
	@ConditionalOnMissingBean(WeekFieldComponentCreator.class)
	public WeekFieldComponentCreator weekFieldComponentCreator() {
		return new WeekFieldComponentCreator();
	}
	
	@Bean
	@Order(1170)
	@ConditionalOnMissingBean(UploadComponentCreator.class)
	public UploadComponentCreator uploadComponentCreator() {
		return new UploadComponentCreator();
	}

	@Bean
	@ConditionalOnMissingBean(value = FieldFactory.class)
	public FieldFactory fieldFactory() {
		return new FieldFactoryImpl();
	}

	@Bean
	@ConditionalOnMissingBean(value = MenuService.class)
	public MenuService menuService() {
		return new MenuService();
	}

	@Bean
	@ConditionalOnMissingBean(value = PermissionChecker.class)
	@ConditionalOnProperty(name = DynamoConstants.SP_ENABLE_VIEW_AUTHORIZATION, havingValue = "true")
	public PermissionChecker permissionChecker(@Value("${ocs.view.package:}") String basePackage) {
		return new DefaultPermissionCheckerImpl(basePackage);
	}

	@Bean
	@ConditionalOnMissingBean(value = AuthorizationServiceInitListener.class)
	@ConditionalOnProperty(name = DynamoConstants.SP_ENABLE_VIEW_AUTHORIZATION, havingValue = "true")
	public AuthorizationServiceInitListener authenticationInitListener() {
		return new AuthorizationServiceInitListener();
	}

	@Bean
	@ConditionalOnMissingBean(value = UserDetailsService.class)
	public UserDetailsService userDetailsService() {
		return new DefaultUserDetailsServiceImpl();
	}

}
