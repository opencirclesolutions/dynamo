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
package com.ocs.dynamo.domain.model.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.layout.HasSelectedItem;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A factory class for generating various input fields based on the entity and
 * attribute models
 * 
 * @author Bas Rutten
 *
 */
@NoArgsConstructor
@Slf4j
public class FieldFactoryImpl implements FieldFactory {

	@Autowired
	private List<ComponentCreator> componentCreators;

	private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	@SuppressWarnings("unchecked")
	public <T, V> void addConvertersAndValidators(HasSelectedItem<T> entityProvider, BindingBuilder<T, V> builder, AttributeModel am,
			FieldCreationContext context, Converter<V, T> customConverter, Function<HasSelectedItem<T>, Validator<?>> customValidator,
			Function<HasSelectedItem<T>, Validator<?>> customRequiredValidator) {

		Optional<ComponentCreator> optCreator = componentCreators.stream().filter(creator -> creator.supports(am, context)).findFirst();
		optCreator.ifPresent(creator -> {
			if (customValidator != null) {
				builder.withValidator((Validator<V>) customValidator.apply(entityProvider));
			}
			if (customConverter != null) {
				if (am.getType().equals(String.class) || NumberUtils.isNumeric(am.getType())) {
					builder.withNullRepresentation((V) "");
				}
				builder.withConverter(customConverter);
			}

			if (customRequiredValidator != null) {
				builder.asRequired((Validator<V>) customRequiredValidator.apply(entityProvider));
			}

			// only delegate to the default mechanism when there are no custom components
			// defined
			if (customConverter == null) {
				creator.addConverters(am, builder);
			}

			if (customValidator == null) {
				creator.addValidators(am, builder);
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <S> DataProvider<S, SerializablePredicate<S>> castSharedProvider(
			DataProvider<?, SerializablePredicate<?>> provider) {
		return (DataProvider<S, SerializablePredicate<S>>) (DataProvider) provider;
	}

	/**
	 * Constructs a field - shortcut method that used the default context
	 * 
	 * @param am the attribute model to base the field on
	 * @return the created component
	 */
	@Override
	public Component constructField(AttributeModel am) {
		return constructField(FieldCreationContext.createDefault(am));
	}

	/**
	 * Constructs a field - this is the main method
	 * 
	 * @param context the context that governs how the field must be created
	 */
	public Component constructField(FieldCreationContext context) {

		AttributeModel am = context.getAttributeModel();

		Map<String, SerializablePredicate<?>> fieldFilters = context.getFieldFilters();
		EntityModel<?> entityModel = context.getFieldEntityModel();

		DataProvider<?, SerializablePredicate<?>> sharedProvider = context.getSharedProvider(am.getPath());

		// for read-only attributes, do not render a field unless it's a link field or
		// the component is inside a search form
		if (EditableType.READ_ONLY.equals(am.getEditableType()) && !AttributeType.DETAIL.equals(am.getAttributeType())
				&& !context.isSearch()) {
			return null;
		}

		SerializablePredicate<?> fieldFilter = fieldFilters == null ? null : fieldFilters.get(am.getPath());
		Component component = findAndInvokeComponentCreator(context, am, entityModel, sharedProvider, fieldFilter);

		if (component != null) {
			postProcessComponent(component, am, context);
		}
		return component;
	}

	/**
	 * Finds the appropriate component creator for this attribute model and context
	 * and uses it to create a component
	 * 
	 * @param context        the component context
	 * @param attributeModel the attribute model
	 * @param entityModel    the entity model
	 * @param sharedProvider shared data provider for use
	 * @param fieldFilter    the field filter to apply
	 * @return the created component
	 */
	private Component findAndInvokeComponentCreator(FieldCreationContext context, AttributeModel attributeModel,
			EntityModel<?> entityModel, DataProvider<?, SerializablePredicate<?>> sharedProvider,
			SerializablePredicate<?> fieldFilter) {

		Optional<ComponentCreator> optCreator = componentCreators.stream().filter(comp -> comp.supports(attributeModel, context))
				.findFirst();
		return optCreator.map(creator -> {
			if (creator instanceof EntityComponentCreator) {
				return invokeEntityComponentCreator(context, attributeModel, entityModel, sharedProvider, fieldFilter,
						creator);
			} else {
				SimpleComponentCreator simpleCreator = (SimpleComponentCreator) creator;
				return simpleCreator.createComponent(attributeModel, context);
			}
		}).orElse(null);
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractEntity<ID>, ID extends Serializable> Component invokeEntityComponentCreator(
			FieldCreationContext context, AttributeModel am, EntityModel<?> entityModel,
			DataProvider<?, SerializablePredicate<?>> sharedProvider, SerializablePredicate<?> fieldFilter,
			ComponentCreator creator) {
		Component field;
		EntityComponentCreator<ID, T> entityCreator = (EntityComponentCreator<ID, T>) creator;
		entityModel = resolveEntityModel(entityModel, am, context);
		BaseService<ID, T> service = (BaseService<ID, T>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());

		field = entityCreator.createComponent(am, context, service, (EntityModel<T>) entityModel,
				(SerializablePredicate<T>) fieldFilter, castSharedProvider(sharedProvider));
		return field;
	}

	/**
	 * Post-processes the component, adding any standard functionality like labels,
	 * tool-tips
	 * 
	 * @param field   the component
	 * @param am      the attribute model to base the component on
	 * @param context the field creation context
	 */
	private void postProcessComponent(Component field, AttributeModel am, FieldCreationContext context) {
		String displayName = am.getDisplayName(VaadinUtils.getLocale());

		VaadinUtils.setLabel(field, context.isEditableGrid() ? "" : displayName);
		VaadinUtils.setTooltip(field, am.getDescription(VaadinUtils.getLocale()));
		VaadinUtils.setPlaceHolder(field, am.getPrompt(VaadinUtils.getLocale()));
		VaadinUtils.setClearButtonVisible(field, am.isClearButtonVisible());

		if (field instanceof AbstractField<?, ?> abstractField) {
			abstractField
					.setRequiredIndicatorVisible(context.isSearch() ? am.isRequiredForSearching() : am.isRequired());
		}

		// right alignment for text field
//		if (NumberUtils.isNumeric(am.getType()) && context.isEditableGrid() && field instanceof TextField) {
//			TextField atf = (TextField) field;
//			atf.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
//		}

		addPercentageSignListener(field, am);
		addCurrencySymbolListener(field, am);
	}

	private void addCurrencySymbolListener(Component field, AttributeModel am) {
		if (am.isCurrency() && field instanceof TextField textField) {
			textField.addBlurListener(event -> {
				String value = textField.getValue();
				if (value != null && !value.contains(am.getCurrencySymbol())) {
					value = am.getCurrencySymbol() + " " + value.trim();
					textField.setValue(value);
				}
			});
		}
	}

	private void addPercentageSignListener(Component field, AttributeModel am) {
		if (am.isPercentage() && field instanceof TextField tf) {
			tf.addBlurListener(event -> {
				String value = tf.getValue();
				if (!StringUtils.isEmpty(value) && value.indexOf('%') < 0) {
					value = value.trim() + "%";
					tf.setValue(value);
				}
			});
		}
	}

	/**
	 * Looks up the entity model to use for a certain field
	 * 
	 * @param entityModel    the base entity model
	 * @param attributeModel the attribute model to use for the file
	 * @param context        the field creation context
	 * @return the entity model
	 */
	private EntityModel<?> resolveEntityModel(EntityModel<?> entityModel, AttributeModel attributeModel,
			FieldCreationContext context) {

		if (entityModel == null) {
			AttributeSelectMode mode = context.getAppropriateMode(attributeModel);

			// lookup fresh model in case of lookup
			if (AttributeSelectMode.LOOKUP.equals(mode)) {
				return serviceLocator.getEntityModelFactory()
						.getModel(attributeModel.getNormalizedType().asSubclass(AbstractEntity.class));
			}

			if (!Boolean.TRUE.equals(context.isSearch()) && attributeModel.getNestedEntityModel() != null) {
				entityModel = attributeModel.getNestedEntityModel();
			} else if (AbstractEntity.class.isAssignableFrom(attributeModel.getNormalizedType())) {
				entityModel = serviceLocator.getEntityModelFactory()
						.getModel(attributeModel.getNormalizedType().asSubclass(AbstractEntity.class));
			} else {
				entityModel = attributeModel.getEntityModel();
			}
		}
		return entityModel;
	}

	@Override
	public <T, V> void addConvertersAndValidators(HasSelectedItem<T> entityProvider, BindingBuilder<T, V> builder, AttributeModel am) {
		addConvertersAndValidators(entityProvider, builder, am, FieldCreationContext.create().build(), null, null, null);
	}

}
