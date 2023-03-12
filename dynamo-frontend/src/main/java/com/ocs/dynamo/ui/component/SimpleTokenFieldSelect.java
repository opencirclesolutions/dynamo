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
package com.ocs.dynamo.ui.component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A token field that displays the distinct values for a basic property of an
 * entity
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <S>  the type of the entity
 * @param <T>  the type of the basic property
 */
public class SimpleTokenFieldSelect<ID extends Serializable, S extends AbstractEntity<ID>, T extends Comparable<T>>
		extends CustomField<Collection<T>> implements Refreshable {

	private static final long serialVersionUID = -1490179285573442827L;

	/**
	 * The attribute model
	 */
	private final AttributeModel attributeModel;

	/**
	 * The name of the field for which to list the distinct values
	 */
	private final String distinctField;

	/**
	 * Whether to take the values from an element collection grid
	 */
	private final boolean elementCollection;

	/**
	 * The type of the element that is being displayed, e.g. String
	 */
	private final Class<T> elementType;

	/**
	 * The entity model
	 */
	private final EntityModel<S> entityModel;

	/**
	 * The field filter
	 */
	private final SerializablePredicate<S> fieldFilter;

	/**
	 * The token field
	 */
	private final MultiSelectComboBox<T> multiComboBox;

	/**
	 * Service for querying the database
	 */
	private final BaseService<ID, S> service;

	public SimpleTokenFieldSelect(BaseService<ID, S> service, EntityModel<S> entityModel, AttributeModel attributeModel,
			SerializablePredicate<S> fieldFilter, String distinctField, Class<T> elementType,
			boolean elementCollection) {
		this.service = service;
		this.entityModel = entityModel;
		this.fieldFilter = fieldFilter;
		this.distinctField = distinctField;
		this.elementType = elementType;
		this.elementCollection = elementCollection;
		this.attributeModel = attributeModel;

		multiComboBox = new MultiSelectComboBox<>();
		if (attributeModel != null) {
			setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()));
			String prompt = attributeModel.getPrompt(VaadinUtils.getLocale());
			if (prompt != null) {
				multiComboBox.setPlaceholder(prompt);
			}
		}
		initContent();
	}

	@Override
	protected Collection<T> generateModelValue() {
		return multiComboBox.getValue();
	}

	@Override
	public Collection<T> getValue() {
		return multiComboBox.getValue();
	}

	protected void initContent() {
		retrieveValues(this.elementCollection);
		multiComboBox.addValueChangeListener(event -> setValue(event.getValue()));
		multiComboBox.setSizeFull();
		add(multiComboBox);
	}

	@Override
	public void refresh() {
		retrieveValues(elementCollection);
	}

	/**
	 * Fills the combo box with the available values
	 * 
	 * @param elementCollection whether to query an element collection
	 */
	private void retrieveValues(boolean elementCollection) {
		List<T> items;
		if (elementCollection) {
			// search element collection table
			items = service.findDistinctInCollectionTable(attributeModel.getCollectionTableName(),
					attributeModel.getCollectionTableFieldName(), elementType);
		} else {
			// search field in regular table
			items = service.findDistinctValues(new FilterConverter<>(entityModel).convert(fieldFilter), distinctField,
					elementType);
		}

		items = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
		items.sort(Comparator.naturalOrder());
		ListDataProvider<T> provider = new ListDataProvider<>(items);

		multiComboBox.setItems(provider);
	}

	@Override
	protected void setPresentationValue(Collection<T> value) {
		if (value == null) {
			value = Collections.emptyList();
		}
		multiComboBox.setValue(new HashSet<>(value));
	}

}
