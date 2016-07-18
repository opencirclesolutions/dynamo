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
package com.ocs.dynamo.service.impl;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.vaadin.server.VaadinSession;

/**
 * Implementation of the simple message service
 * 
 * @author bas.rutten
 */
public class MessageServiceImpl implements MessageService {

    private static final String MESSAGE_NOT_FOUND = "[Warning: message '%s' not found]";

    private static final Logger LOG = Logger.getLogger(MessageServiceImpl.class);

    @Inject
    private MessageSource source;

    @Override
    public String getAttributeMessage(String reference, AttributeModel attributeModel,
            String propertyName) {
        if (source != null) {
            try {
                String messageName = reference + "." + attributeModel.getName() + "."
                        + propertyName;
                return source.getMessage(messageName, null, getLocale());
            } catch (NoSuchMessageException ex) {
                // do nothing
                return null;
            }
        }
        return null;
    }

    @Override
    public String getEntityMessage(String reference, String propertyName) {
        if (source != null) {
            try {
                String messageName = reference + "." + propertyName;
                return source.getMessage(messageName, null, getLocale());
            } catch (NoSuchMessageException ex) {
                // do nothing
                return null;
            }
        }
        return null;
    }

    @Override
    public <E extends Enum<?>> String getEnumMessage(Class<E> enumClass, E value) {
        return value == null ? null : getMessage(enumClass.getSimpleName() + "." + value.name());
    }

    private Locale getLocale() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return session.getLocale();
        }
        return Locale.getDefault();
    }

    @Override
    public String getMessage(String key) {
        return getMessage(key, getLocale());
    }

    @Override
    public String getMessage(String key, Object... args) {
        return getMessage(key, getLocale(), args);
    }

    @Override
    public String getMessage(String key, Locale locale, Object... args) {
        try {
            return source.getMessage(key, args, locale);
        } catch (NoSuchMessageException ex) {
            LOG.error(ex.getMessage());
            return String.format(MESSAGE_NOT_FOUND, key);
        }
    }

    @Override
    public String getMessageNoDefault(String key) {
        return getMessageNoDefault(key, getLocale());
    }

    @Override
    public String getMessageNoDefault(String key, Object... args) {
        return getMessageNoDefault(key, getLocale(), args);
    }

    @Override
    public String getMessageNoDefault(String key, Locale locale, Object... args) {
        try {
            return source.getMessage(key, args, getLocale());
        } catch (NoSuchMessageException ex) {
            return null;
        }
    }

}
