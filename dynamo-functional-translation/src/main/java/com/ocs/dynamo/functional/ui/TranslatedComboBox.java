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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
 
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.filter.FilterUtil;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.functional.domain.TranslationService;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.filter.And;
import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

/**
 * @author patrickdeenen
 * 
 *         Translation aware combobox which queries the entity for ids and uses the ids to query the translations.
 * 
 *         The locale can be set by using a filter to the locale, otherwise the locale from the session will be used.
 *
 */
public class TranslatedComboBox<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomEntityField<ID, T, T> {

	private static final long serialVersionUID = 3044650211099305631L;
	private static final String TRANSLATION_PROPERTY = "translations.locale";
	private Filter originalFilter;
	private Filter additionalFilter;
	private ComboBox comboBox;
	
	public TranslatedComboBox(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			Filter filter) {
		super(service, entityModel, attributeModel, filter);

		comboBox = new ComboBox();
		if (getAttributeModel() != null) {
			comboBox.setCaption(getAttributeModel().getDisplayName());
		}
		comboBox.setRequiredError(getMessageService().getMessage("ocs.may.not.be.null", VaadinUtils.getLocale()));
		comboBox.setFilteringMode(FilteringMode.CONTAINS);
		comboBox.addContainerProperty("key", Integer.class, -1);
		comboBox.addContainerProperty("translation", String.class, "");
		comboBox.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		comboBox.setItemCaptionPropertyId("translation");

		// Select value
		comboBox.addValueChangeListener(event -> {
			Item item = comboBox.getItem(event.getProperty().getValue());
			if (item != null) {
				T entity = getService().fetchById((ID) item.getItemProperty("key").getValue());
				setValue(entity);
			} else {
				setValue(null);
			}
		});

		refresh();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Component initContent() {
		return comboBox;
	}

	@Override
	public Class<? extends T> getType() {
		return getEntityModel().getEntityClass();
	}
	
	/**
	 * @return the comboBox
	 */
	public ComboBox getComboBox() {
		return comboBox;
	}

	@Override
	public void clearAdditionalFilter() {
		this.additionalFilter = null;
		setFilter( originalFilter);
		refresh();
	}

	@Override
	public Filter getAdditionalFilter() {
		return additionalFilter;
	}

	@Override
	public void setAdditionalFilter(Filter additionalFilter) {
		setValue(null);
		this.additionalFilter = additionalFilter;
		setFilter( originalFilter == null ? additionalFilter : new And(originalFilter, additionalFilter));
		refresh();
	}

	@SuppressWarnings("unchecked")
	public void refresh() {
		comboBox.removeAllItems();
		// Try to find locale in filter
		String locale = VaadinUtils.getLocale().toString();
		com.ocs.dynamo.filter.Filter cf = new FilterConverter(null).convert(getFilter());
		if (cf != null) {
			// Try to extract locale filter
			com.ocs.dynamo.filter.Filter localeFilter = FilterUtil.extractFilter(cf, TRANSLATION_PROPERTY);
			if (localeFilter != null) {
				// When found remove from entity query
				FilterUtil.removeFilters(cf, TRANSLATION_PROPERTY);
				// And use in translation query
				Object value = FilterUtil.extractFilterValue(getFilter(), TRANSLATION_PROPERTY);
				if (value != null) {
					if (value instanceof Locale) {
						locale = ((Locale) value).toLanguageTag();
					} else if (value instanceof com.ocs.dynamo.functional.domain.Locale) {
						locale = ((com.ocs.dynamo.functional.domain.Locale) value).getCode();
					} else {
						locale = value.toString();
					}
				}
			}
		}

		// Query ids
		List<Integer> ids = (List<Integer>) getService().findIds(cf);

		// Query translations
		TranslationService ts = (TranslationService) ServiceLocatorFactory.getServiceLocator()
				.getServiceForEntity(Translation.class);
		List<Object[]> result = ts.fetchByIds(getEntityModel().getEntityClass(), getEntityModel().getDisplayProperty(),
				locale, ids);
		// Fill combo with translations
		for (Object[] row : result) {
			Item newItem = comboBox.addItem(row[0]);
			newItem.getItemProperty("key").setValue(row[0]);
			newItem.getItemProperty("translation").setValue(row[1]);
		}
	}

	@Override
	public void refresh(Filter filter) {
		this.originalFilter = filter;
		setFilter( filter);
		refresh();
	}

	@Override
	protected void setInternalValue(T newValue) {
		super.setInternalValue(newValue);
		if (comboBox != null) {
			comboBox.select(newValue != null ? newValue.getId() : null);
		}
	}

	@Override
	public void setValue(T newFieldValue) {
		super.setValue(newFieldValue);
		if (comboBox != null) {
			comboBox.select(newFieldValue != null ? newFieldValue.getId() : null);
		}
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		if (comboBox != null) {
			comboBox.setComponentError(componentError);
		}
	}
}
