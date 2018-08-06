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
package com.ocs.dynamo.functional.domain;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.utils.ClassUtils;
import org.hibernate.annotations.DiscriminatorOptions;

/**
 * Base class for entities that contain a collection of Translations
 *
 * @author Patrick.Deenen@opencircle.solutions
 *
 */
@SuppressWarnings("rawtypes")
@MappedSuperclass
@DiscriminatorOptions(force=true)
public abstract class AbstractEntityTranslated<ID, T extends Translation>
		extends AbstractAuditableEntity<ID> {

	private static final long serialVersionUID = 511877206448482880L;

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE,
			CascadeType.PERSIST }, mappedBy = "entity", orphanRemoval = true)
	@Attribute(visible = VisibilityType.HIDE)
	private Set<T> translations = new HashSet<>();

	public Set<T> getTranslations() {
		return translations;
	}

	public void setTranslations(final Set<T> translations) {
		this.translations = translations;
	}

	public T getTranslations(final String field, final Locale locale) {
		return getTranslations(field, locale == null ? null : locale.getCode());
	}

	/**
	 * Gets all translations for a certain field for the provided locale
	 *
	 * @param field
	 *            the field
	 * @param locale
	 *            the (code of) the locale
	 * @return
	 */
	public T getTranslations(final String field, final String locale) {
		T translation = null;
		Set<T> translations = getTranslations(field);
		if (translations != null && !translations.isEmpty()) {
			if (locale == null || "".equals(locale)) {
				translation = translations.iterator().next();
			} else {
				for (T t : translations) {
					if (locale.equalsIgnoreCase(t.getLocale().getCode())) {
						translation = t;
						break;
					}
				}
			}
		}
		return translation;
	}

	/**
	 * Gets all translations for a certain field
	 *
	 * @param field
	 *            the name of the field
	 * @return
	 */
	public Set<T> getTranslations(final String field) {
		Set<T> translations = null;
		if (field != null && !"".equals(field)) {
			translations = new HashSet<>();
			for (final T translation : getTranslations()) {
				if (field.equalsIgnoreCase(translation.getField())) {
					translations.add(translation);
				}
			}
		}
		return translations;
	}

	/**
	 * Sets all translations for a field
	 *
	 * @param field
	 *            the name of the field
	 * @param translations
	 *            the translations
	 */
	public void setTranslations(final String field, final Set<T> translations) {
		if (field != null && !"".equals(field)) {
			for (final T translation : getTranslations()) {
				if (field.equalsIgnoreCase(translation.getField())) {
					this.removeTranslation(translation);
				}
			}
			for (final T translation : translations) {
				this.addTranslation(translation);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addTranslation(final T translation) {
		this.translations.add(translation);
		translation.setEntity(this);
	}

	@SuppressWarnings("unchecked")
	public void removeTranslation(final T translation) {
		this.translations.remove(translation);
		translation.setEntity(null);
	}

	@AssertTrue(message = "{ocs.not.all.translations.provided}")
	protected boolean isValidRequiredTranslations() {
		final Collection<Locale> requiredLocales = getRequiredLocales();
		final Collection<String> requiredTranslatedFields = getRequiredTranslatedFields();
		if (requiredLocales == null || requiredTranslatedFields == null) {
			return true;
		}
		for (Locale requiredLocale: requiredLocales) {
			for (String requiredTranslatedField : requiredTranslatedFields) {
				if (getTranslations(requiredTranslatedField, requiredLocale) == null) {
					return false;
				}
			}

		}
		return true;
	}

	@AssertTrue(message = "{ocs.multiple.translations.provided}")
	protected boolean isTranslationsDoesNotContainDuplicates() {
		final Collection<String> requiredTranslatedFields = getRequiredTranslatedFields();
		if (requiredTranslatedFields == null) {
			return true;
		}
		for (String requiredTranslatedField : requiredTranslatedFields) {
			Set<T> translations = getTranslations(requiredTranslatedField);
			Set<Locale> uniqueLocales = translations.stream().map(translation -> translation.getLocale()).collect(Collectors.toSet());
			if (translations.size() != uniqueLocales.size()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @return the required locales
	 */
	protected Collection<Locale> getRequiredLocales() {
		return new HashSet<>();
	}

	/**
	 * 
	 * @return the translated fields that are required
	 */
	protected Collection<String> getRequiredTranslatedFields() {
		return new HashSet<>();
	}
}
