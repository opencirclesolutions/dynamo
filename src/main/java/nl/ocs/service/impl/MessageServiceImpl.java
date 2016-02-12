package nl.ocs.service.impl;

import java.util.Locale;

import javax.inject.Inject;

import nl.ocs.domain.model.AttributeModel;
import nl.ocs.service.MessageService;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.vaadin.server.VaadinSession;

/**
 * Implementation of the simple message service
 * 
 * @author bas.rutten
 * 
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
			LOG.error(ex.getMessage(), ex);
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
