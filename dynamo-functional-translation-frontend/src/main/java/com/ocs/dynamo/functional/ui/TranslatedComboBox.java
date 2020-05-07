///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.functional.ui;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import com.ocs.dynamo.domain.AbstractEntity;
//import com.ocs.dynamo.domain.model.AttributeModel;
//import com.ocs.dynamo.domain.model.EntityModel;
//import com.ocs.dynamo.filter.AndPredicate;
//import com.ocs.dynamo.filter.Compare;
//import com.ocs.dynamo.filter.DynamoFilterUtil;
//import com.ocs.dynamo.filter.Filter;
//import com.ocs.dynamo.filter.FilterConverter;
//import com.ocs.dynamo.functional.domain.Translation;
//import com.ocs.dynamo.functional.domain.TranslationService;
//import com.ocs.dynamo.service.BaseService;
//import com.ocs.dynamo.service.ServiceLocatorFactory;
//import com.ocs.dynamo.ui.Refreshable;
//import com.ocs.dynamo.ui.component.CustomEntityField;
//import com.ocs.dynamo.ui.utils.VaadinUtils;
//import com.ocs.dynamo.utils.ClassUtils;
//import com.vaadin.data.provider.ListDataProvider;
//import com.vaadin.server.ErrorMessage;
//import com.vaadin.server.SerializablePredicate;
//import com.vaadin.shared.Registration;
//import com.vaadin.ui.ComboBox;
//import com.vaadin.ui.Component;
//
///**
// * @author patrickdeenen
// * 
// *         Translation aware combobox which queries the entity for ids and uses
// *         the ids to query the translations.
// * 
// *         The locale can be set by using a filter to the locale, otherwise the
// *         locale from the session will be used.
// *
// */
//public class TranslatedComboBox<ID extends Serializable, T extends AbstractEntity<ID>>
//		extends CustomEntityField<ID, T, T> implements Refreshable {
//
//	private static final long serialVersionUID = 3044650211099305631L;
//
//	private static final String TRANSLATION_PROPERTY = "translations.locale";
//
//	private SerializablePredicate<T> originalFilter;
//
//	private SerializablePredicate<T> additionalFilter;
//
//	private ComboBox<TranslationPair<ID>> comboBox;
//
//	private ListDataProvider<TranslationPair<ID>> dataProvider;
//
//	private TranslationService ts = (TranslationService) ServiceLocatorFactory.getServiceLocator()
//			.getServiceForEntity(Translation.class);
//
//	private boolean changing = false;
//
//	public TranslatedComboBox(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
//			SerializablePredicate<T> filter) {
//		super(service, entityModel, attributeModel, filter);
//
//		dataProvider = new ListDataProvider<>(new ArrayList<>());
//		comboBox = new ComboBox<>();
//		comboBox.setDataProvider(dataProvider);
//		if (getAttributeModel() != null) {
//			comboBox.setCaption(getAttributeModel().getDisplayName(VaadinUtils.getLocale()));
//		}
//		// comboBox.setRequiredError(getMessageService().getMessage("ocs.may.not.be.null",
//		// VaadinUtils.getLocale()));
//		// comboBox.addContainerProperty("key", Integer.class, -1);
//		// comboBox.addContainerProperty("translation", String.class, "");
//		// comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
//		// comboBox.setItemCaptionPropertyId("translation");
//		comboBox.setItemCaptionGenerator(t -> ClassUtils.getFieldValueAsString(t, "translation"));
//		comboBox.setSizeFull();
//
//		// Select value
////		comboBox.addValueChangeListener(event -> {
////			changing = true;
////			TranslationPair<ID> pair = event.getValue();
////			if (pair != null) {
////				T entity = getService().fetchById(pair.getKey());
////				setValue(entity);
////			} else {
////				clear();
////			}
////			changing = false;
////		});
//
//		refresh();
//	}
//
//	@Override
//	public Registration addValueChangeListener(ValueChangeListener<T> listener) {
//		return comboBox.addValueChangeListener(event -> {
//			listener.valueChange(new ValueChangeEvent<T>(this, null, true));
//		});
//	}
//
//	@Override
//	protected Component initContent() {
//		return comboBox;
//	}
//
//	/**
//	 * @return the comboBox
//	 */
//	public ComboBox<TranslationPair<ID>> getComboBox() {
//		return comboBox;
//	}
//
//	@Override
//	public void clearAdditionalFilter() {
//		this.additionalFilter = null;
//		setFilter(originalFilter);
//		refresh();
//	}
//
//	@Override
//	public SerializablePredicate<T> getAdditionalFilter() {
//		return additionalFilter;
//	}
//
//	@Override
//	public void setAdditionalFilter(SerializablePredicate<T> additionalFilter) {
//		setValue(null);
//		this.additionalFilter = additionalFilter;
//		setFilter(originalFilter == null ? new AndPredicate<T>(additionalFilter)
//				: new AndPredicate<T>(originalFilter, additionalFilter));
//		refresh();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public void refresh() {
//		dataProvider.getItems().clear();
//		// Try to find locale in filter
//		String locale = VaadinUtils.getLocale().toString();
//		Filter cf = new FilterConverter<T>(getEntityModel()).convert(getFilter());
//		if (cf != null) {
//			// Try to extract locale filter
//			Filter localeFilter = DynamoFilterUtil.extractFilter(cf, TRANSLATION_PROPERTY);
//			if (localeFilter != null) {
//				// When found remove from entity query
//				DynamoFilterUtil.removeFilters(cf, TRANSLATION_PROPERTY);
//				// And use in translation query
//				Filter extracted = DynamoFilterUtil.extractFilter(cf, TRANSLATION_PROPERTY);
//				if (extracted != null) {
//					Compare.Equal pf = (Compare.Equal) extracted;
//					if (pf.getValue() instanceof java.util.Locale) {
//						locale = ((Locale) pf.getValue()).toLanguageTag();
//					} else if (pf.getValue() instanceof com.ocs.dynamo.functional.domain.Locale) {
//						locale = ((com.ocs.dynamo.functional.domain.Locale) pf.getValue()).getCode();
//					} else {
//						locale = pf.getValue().toString();
//					}
//				}
//			}
//		}
//
//		// Query ids
//		List<Integer> ids = (List<Integer>) getService().findIds(cf);
//
//		// Query translations
//
//		List<Object[]> result = (List<Object[]>) ts.fetchByIds(getEntityModel().getEntityClass(),
//				getEntityModel().getDisplayProperty(), locale, ids);
//		// Fill combo with translations
//		for (Object[] row : result) {
//			TranslationPair<ID> p = new TranslationPair<ID>((ID) row[0], (String) row[1]);
//			dataProvider.getItems().add(p);
//		}
//	}
//
//	@Override
//	public void refresh(SerializablePredicate<T> filter) {
//		this.originalFilter = filter;
//		setFilter(filter);
//		refresh();
//	}
//
////	@Override
////	protected void setInternalValue(T newValue) {
////		super.setInternalValue(newValue);
////		if (comboBox != null) {
////			comboBox.select(newValue != null ? newValue.getId() : null);
////		}
////	}
//
//	@Override
//	public void setValue(T newFieldValue) {
//		super.setValue(newFieldValue);
//		if (comboBox != null && !changing) {
//			comboBox.setValue(newFieldValue == null ? null : new TranslationPair<ID>(newFieldValue.getId(), "test"));
//		}
//	}
//
//	@Override
//	public void setComponentError(ErrorMessage componentError) {
//		if (comboBox != null) {
//			comboBox.setComponentError(componentError);
//		}
//	}
//
//	@Override
//	public T getValue() {
//		if (comboBox != null) {
//			TranslationPair<ID> pair = comboBox.getValue();
//			return pair == null ? null : getService().fetchById(pair.getKey());
//		}
//		return null;
//	}
//
//	@Override
//	protected void doSetValue(T value) {
//
//	}
//}
