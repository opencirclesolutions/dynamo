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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.validation.constraints.AssertTrue;

import org.hibernate.annotations.DiscriminatorOptions;

import com.ocs.dynamo.domain.AbstractAuditableEntity;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;

/**
 * Base class for entities that contain a collection of Translations
 *
 * @author Patrick.Deenen@opencircle.solutions
 *
 */
@SuppressWarnings("rawtypes")
@MappedSuperclass
@DiscriminatorOptions(force = true)
public abstract class AbstractEntityTranslated<ID, T extends Translation> extends AbstractAuditableEntity<ID> {

    private static final long serialVersionUID = 511877206448482880L;

    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST }, mappedBy = "entity", orphanRemoval = true)
    @Attribute(visible = VisibilityType.HIDE)
    private Set<T> translations = new HashSet<>();

    @SuppressWarnings("unchecked")
    public void addTranslation(T translation) {
        this.translations.add(translation);
        translation.setEntity(this);
    }

    /**
     * 
     * @return the required locales
     */
    public Collection<Locale> getRequiredLocales() {
        return new HashSet<>();
    }

    /**
     * 
     * @return the translated fields that are required
     */
    protected Collection<String> getRequiredTranslatedFields() {
        return new HashSet<>();
    }

    /**
     *
     * @return the translated fields that should be rendered as text area instead of
     *         text field
     */
    public Collection<String> getTextAreaFields() {
        return new HashSet<>();
    }

    /**
     * Gets the translations for a field in the specified locale
     * 
     * @param field  the field
     * @param locale the locale
     * @return
     */
    public T getTranslation(String field, Locale locale) {
        return getTranslation(field, locale == null ? null : locale.getCode());
    }

    /**
     * Gets the translations for a field for the provided locale
     *
     * @param field  the field
     * @param locale the (code of) the locale
     * @return
     */
    public T getTranslation(String field, String locale) {
        if (locale == null) {
            return null;
        }

        T translation = null;
        Set<T> translations = getTranslations(field);
        if (translations != null && !translations.isEmpty()) {
            translation = translations.stream().filter(t -> t.getLocale() != null && t.getLocale().getCode().equalsIgnoreCase(locale))
                    .findFirst().orElse(null);
        }
        return translation;
    }

    public Set<T> getTranslations() {
        return translations;
    }

    /**
     * Gets all translations for all locales for the specified field
     *
     * @param field the name of the field
     * @return
     */
    public Set<T> getTranslations(String field) {
        return translations.stream().filter(t -> t.getField().equalsIgnoreCase(field)).collect(Collectors.toSet());
    }

    /**
     * 
     * @return check that the translation set for a field does not contain
     *         duplicates
     */
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
     * @return whether all required translations are present
     */
    @AssertTrue(message = "{ocs.not.all.translations.provided}")
    protected boolean isValidRequiredTranslations() {
        final Collection<Locale> requiredLocales = getRequiredLocales();
        final Collection<String> requiredTranslatedFields = getRequiredTranslatedFields();
        if (requiredLocales == null || requiredTranslatedFields == null) {
            return true;
        }
        for (Locale requiredLocale : requiredLocales) {
            for (String requiredTranslatedField : requiredTranslatedFields) {
                if (getTranslation(requiredTranslatedField, requiredLocale) == null) {
                    return false;
                }
            }

        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public void removeTranslation(final T translation) {
        this.translations.remove(translation);
        translation.setEntity(null);
    }

    public void setTranslations(final Set<T> translations) {
        this.translations = translations;
    }

    /**
     * Sets all translations for a field
     *
     * @param field        the name of the field
     * @param translations the translations
     */
    public void setTranslations(String field, Set<T> translations) {
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

}
