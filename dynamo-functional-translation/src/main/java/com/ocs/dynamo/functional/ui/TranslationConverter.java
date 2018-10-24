/**
 * 
 */
package com.ocs.dynamo.functional.ui;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.ocs.dynamo.functional.domain.Translation;
import com.vaadin.v7.data.util.converter.Converter;

/**
 * @author patrickdeenen
 *
 */
public class TranslationConverter implements Converter<Translation<?>, Set<Translation<?>>> {

	public TranslationConverter() {
		// default constructor
	}

	@Override
	public Set<Translation<?>> convertToModel(Translation<?> value, Class<? extends Set<Translation<?>>> targetType,
			Locale locale) throws ConversionException {
		if (value == null) {
			return null;
		}
		Set<Translation<?>> r = new HashSet<>();
		r.add(value);
		return r;
	}

	@Override
	public Translation<?> convertToPresentation(Set<Translation<?>> value, Class<? extends Translation<?>> targetType,
			Locale locale) throws ConversionException {
		if (value == null) {
			return null;
		}
		Translation<?> result = null;
		for (Translation<?> t : value) {
			if (t.getLocale().getCode().equals(locale.toString())) {
				result = t;
				break;
			}
		}
		if (result == null && !value.isEmpty()) {
			result = value.iterator().next();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Set<Translation<?>>> getModelType() {
		return (Class<Set<Translation<?>>>) (Object) Set.class;
	}

	@Override
	public Class<Translation<?>> getPresentationType() {
		return (Class<Translation<?>>) (Object) Translation.class;
	}

}
