package org.dynamoframework.service.impl;

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

import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

/**
 * Implementation of the simple message service
 *
 * @author bas.rutten
 */
@Slf4j
public class MessageServiceImpl implements MessageService {

	private static final String MESSAGE_NOT_FOUND = "[Warning: message '%s' not found]";

	@Autowired
	private MessageSource source;

	@Override
	public String getAttributeMessage(String reference, AttributeModel attributeModel, String propertyName, Locale locale) {
		if (source != null) {
			try {
				String messageName = reference + "." + attributeModel.getName() + "." + propertyName;
				return source.getMessage(messageName, null, locale);
			} catch (NoSuchMessageException ex) {
				// do nothing
				return null;
			}
		}
		return null;
	}

	@Override
	public String getEntityMessage(String reference, String propertyName, Locale locale) {
		if (source != null) {
			try {
				String messageName = reference + "." + propertyName;
				return source.getMessage(messageName, null, locale);
			} catch (NoSuchMessageException ex) {
				// do nothing
				return null;
			}
		}
		return null;
	}

	@Override
	public <E extends Enum<?>> String getEnumMessage(Class<E> enumClass, E value, Locale locale) {
		return value == null ? null : getMessage(enumClass.getSimpleName() + "." + value.name(), locale);
	}

	@Override
	public String getMessage(String key, Locale locale, Object... args) {
		try {
			return source.getMessage(key, args, locale);
		} catch (NoSuchMessageException ex) {
			log.error(ex.getMessage());
			return String.format(MESSAGE_NOT_FOUND, key);
		}
	}

	@Override
	public String getMessageNoDefault(String key, Locale locale) {
		return getMessageNoDefault(key, locale, new Object[0]);
	}

	@Override
	public String getMessageNoDefault(String key, Locale locale, Object... args) {
		try {
			return source.getMessage(key, args, locale);
		} catch (NoSuchMessageException ex) {
			return null;
		}
	}

}
