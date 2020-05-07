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
package com.ocs.dynamo.functional.ui;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Map;

/**
 * This factory can be used to generate TranslationTable objects for attributes
 * in fields which have to be dynamically localized (translated). It expects a
 * generic database table based on entity Translation and attributes to be
 * translated mapped to the translation collection in this entity.
 *
 * This class can be used in 2 ways: [1] by hand [2] as a factory delegate as
 * part of the editform.
 *
 * @author patrick.deenen@opencircle.solutions
 *
 */
public class TranslationFieldFactory implements FieldFactory {

    private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

    /**
     * Default constructor
     */
    public TranslationFieldFactory() {
    }

    /**
     * Adds converters and validators for a field
     *
     * @param builder                 the binding builder to which to add the converters and
     *                                validators
     * @param am                      the attribute model for the field
     * @param customConverter         custom converter to be used for data conversion
     * @param customValidator
     * @param customRequiredValidator
     */
    @Override
    public <U, V> void addConvertersAndValidators(Binder.BindingBuilder<U, V> builder, AttributeModel am, Converter<V, U> customConverter, Validator<V> customValidator, Validator<V> customRequiredValidator) {

    }

    @Override
    public Component constructField(AttributeModel am) {
        return constructField(FieldFactoryContext.createDefault(am));
    }

    @Override
    public Component constructField(FieldFactoryContext context) {
        AttributeModel am = context.getAttributeModel();
        if (am.isVisible()) {

            Map<String, SerializablePredicate<?>> fieldFilters = context.getFieldFilters();
            SerializablePredicate<?> fieldFilter = fieldFilters == null ? null : fieldFilters.get(am.getPath());
            if (fieldFilter != null && (AbstractEntityTranslated.class.isAssignableFrom(am.getType())
                    || AbstractEntityTranslated.class.isAssignableFrom(am.getNestedEntityModel().getEntityClass()))) {

                // construct combo box
                EntityModel<?> entityModel = (EntityModel<?>) resolveEntityModel(context.getFieldEntityModel(), am, context.isSearch());
                BaseService<?, ?> service = (BaseService<?, ?>) serviceLocator.getServiceForEntity(entityModel.getEntityClass());
                // return new TranslatedComboBox<ID, T>(service, entityModel, am,
                // (SerializablePredicate<T>) fieldFilter);
                return null;
            } else if (am.getNestedEntityModel() != null && Translation.class.isAssignableFrom(am.getNestedEntityModel().getEntityClass())
                    && context.getParentEntity() != null) {
                return constructGrid(context, am);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <ID, E extends AbstractEntityTranslated<ID, Translation<E>>> TranslationGrid<ID, E> constructGrid(FieldFactoryContext context,
                                                                                                              AttributeModel am) {
        final EntityModel<Translation<E>> nem = (EntityModel<Translation<E>>) serviceLocator.getEntityModelFactory()
                .getModel(am.getNestedEntityModel().getEntityClass());
        TranslationGrid<ID, E> tt = new TranslationGrid<ID, E>((E) context.getParentEntity(), am.getName(), nem, am, context.isViewMode(),
                am.isLocalesRestricted());
        tt.setRequiredIndicatorVisible(am.isRequired());
        tt.setLabel(am.getDisplayName(VaadinUtils.getLocale()));
        return tt;
    }

    /**
     * Resolves an entity model by falling back first to the nested attribute model
     * and then to the default model for the normalized type of the property
     *
     * @param entityModel    the entity model
     * @param attributeModel the attribute model
     * @param search
     * @return
     */
    private EntityModel<?> resolveEntityModel(EntityModel<?> entityModel, AttributeModel attributeModel, Boolean search) {
        if (entityModel == null) {
            if (!Boolean.TRUE.equals(search) && attributeModel.getNestedEntityModel() != null) {
                entityModel = attributeModel.getNestedEntityModel();
            } else {
                Class<?> type = attributeModel.getNormalizedType();
                entityModel = serviceLocator.getEntityModelFactory().getModel(type.asSubclass(AbstractEntity.class));
            }
        }
        return entityModel;
    }


}
