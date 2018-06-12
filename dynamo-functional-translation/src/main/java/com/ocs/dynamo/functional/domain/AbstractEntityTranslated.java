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

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.validation.constraints.AssertTrue;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * Base class for entities that contain a collection of Translations
 *
 * @author Patrick.Deenen@opencircle.solutions
 *
 */
@SuppressWarnings("rawtypes")
@MappedSuperclass
public abstract class AbstractEntityTranslated<ID, T extends Translation>
		extends AbstractAuditableEntity<ID> {

	private static final long serialVersionUID = 511877206448482880L;

	@OneToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE,
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
		boolean result = false;
		Collection<String> locales = getRequiredLocales();
		if (locales == null) {
			result = true;
		} else {
			// Initialize counts
			String[] la = locales.toArray(new String[] {});
			HashMap<String, Integer> countTranslations = new HashMap<>();
			// Count the nb of translations for each field
			for (T t : getTranslations()) {
				for (int i = 0; i < la.length; i++) {
					if (la[i].equalsIgnoreCase(t.getLocale().getCode())) {
						if (!countTranslations.containsKey(t.getField())) {
							countTranslations.put(t.getField(), new Integer(1));
						} else {
							Integer cnt = countTranslations.get(t.getField());
							countTranslations.put(t.getField(), ++cnt);
						}
						break;
					}
				}
			}
			// When not all fields have been found then not valid
			List<String> tf = findTranslatedFields();
			if (!countTranslations.keySet().isEmpty() && countTranslations.keySet().size() == tf.size()) {
				result = true;
				// When not every field has the required translations then not valid
				for (String fn : countTranslations.keySet()) {
					if (la.length != countTranslations.get(fn)) {
						// Not valid
						result = false;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @return the required locales
	 */
	protected Collection<String> getRequiredLocales() {
		return null;
	}

	/**
	 * 
	 * @return ??
	 */
	protected List<String> findTranslatedFields() {
		Method[] methods = this.getClass().getDeclaredMethods();
		ArrayList<String> translatedFields = new ArrayList<>();
		for (Method m : methods) {
			if (m.getName().startsWith("get") && Collection.class.isAssignableFrom(m.getReturnType())) {
				Class<?> ta = ClassUtils.getResolvedType(m, 0);
				if (ta != null && Translation.class.isAssignableFrom(ta)) {
					translatedFields.add(m.getName().substring(3).toUpperCase());
				}
			}
		}
		return translatedFields;
	}
}
