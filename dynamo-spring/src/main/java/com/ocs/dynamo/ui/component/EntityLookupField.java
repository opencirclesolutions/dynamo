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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.util.EntityModelUtil;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.utils.StringUtil;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * A composite component that displays a selected entity and offers a search dialog to search for
 * another one
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 * @param <Object>
 *            the type of the selected value (can be a single entity or a collection depending on
 *            the use case)
 */
public class EntityLookupField<ID extends Serializable, T extends AbstractEntity<ID>> extends
        QuickAddEntityField<ID, T, Object> {

	private static final long serialVersionUID = 5377765863515463622L;

	/**
	 * Indicates whether it is allowed to add items
	 */
	private boolean addAllowed;

	/**
	 * The button used to clear the current selection
	 */
	private Button clearButton;

	/**
	 * The filters to apply to the search dialog
	 */
	private List<Filter> filters;

	/**
	 * The joins to apply to the search in the search dialog
	 */
	private final FetchJoinInformation[] joins;

	/**
	 * The label that displays the currently selected item
	 */
	private Label label;

	/**
	 * Whether the component allows multiple select
	 */
	private boolean multiSelect;

	/**
	 * The page length of the table in the search dialog
	 */
	private Integer pageLength;

	/**
	 * The button that brings up the search dialog
	 */
	private Button selectButton;

	/**
	 * The sort order to apply to the search dialog
	 */
	private List<SortOrder> sortOrders = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to query the database
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute mode
	 * @param filters
	 *            the filter to apply when searching
	 * @param search
	 *            whether the component is used in a search screen
	 * @param sortOrder
	 *            the sort order
	 * @param joins
	 *            the joins to use when fetching data when filling the popop dialog
	 */
	public EntityLookupField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
	        List<Filter> filters, boolean search, boolean multiSelect, List<SortOrder> sortOrders,
	        FetchJoinInformation... joins) {
		super(service, entityModel, attributeModel);
		this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<SortOrder>();
		this.filters = filters;
		this.joins = joins;
		this.multiSelect = multiSelect;
		this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
	}

	/**
	 * Adds a sort order
	 * 
	 * @param sortOrder
	 *            the sort order to add
	 */
	public void addSortOrder(SortOrder sortOrder) {
		this.sortOrders.add(sortOrder);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void afterNewEntityAdded(T entity) {
		if (multiSelect) {
			if (getValue() == null) {
				// create new collection
				setValue(Lists.newArrayList(entity));
			} else {
				// add new entity to existing collection
				Collection<T> col = (Collection<T>) getValue();
				col.add(entity);
				setValue(col);
			}
		} else {
			setValue(entity);
		}
	}

	public Button getClearButton() {
		return clearButton;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public Integer getPageLength() {
		return pageLength;
	}

	public Button getSelectButton() {
		return selectButton;
	}

	public List<SortOrder> getSortOrders() {
		return Collections.unmodifiableList(sortOrders);
	}

	@Override
	public Class<? extends Object> getType() {
		return Object.class;
	}

	@Override
	protected Component initContent() {
		HorizontalLayout bar = new DefaultHorizontalLayout(false, true, true);
		if (this.getAttributeModel() != null) {
			this.setCaption(getAttributeModel().getDisplayName());
		}

		// label for displaying selected values
		label = new Label();
		updateLabel(getValue());
		bar.addComponent(label);

		// button for selecting an entity - brings up the search dialog
		selectButton = new Button(getMessageService().getMessage("ocs.select"));
		selectButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 8377632639548698729L;

			@Override
			public void buttonClick(ClickEvent event) {
				ModelBasedSearchDialog<ID, T> dialog = new ModelBasedSearchDialog<ID, T>(getService(),
				        getEntityModel(), filters, sortOrders, multiSelect, true, joins) {

					private static final long serialVersionUID = -3432107069929941520L;

					@Override
					@SuppressWarnings("unchecked")
					protected boolean doClose() {
						if (multiSelect) {
							if (getValue() == null) {
								setValue(getSelectedItems());
							} else {
								// get current value
								Collection<T> current = (Collection<T>) EntityLookupField.this.getValue();
								// add new values
								for (T t : getSelectedItems()) {
									if (!current.contains(t)) {
										current.add(t);
									}
								}
								EntityLookupField.this.setValue(current);
							}
						} else {
							// single value select
							setValue(getSelectedItem());
						}
						return true;
					}
				};
				dialog.setPageLength(pageLength);
				dialog.build();
				getUi().addWindow(dialog);
			}
		});
		bar.addComponent(selectButton);

		// button for clearing the current selection
		clearButton = new Button(getMessageService().getMessage("ocs.clear"));
		clearButton.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 8377632639548698729L;

			@Override
			public void buttonClick(ClickEvent event) {
				setValue(null);
			}
		});
		bar.addComponent(clearButton);

		// quick add button
		if (addAllowed) {
			Button addButton = constructAddButton();
			bar.addComponent(addButton);
		}
		return bar;
	}

	/**
	 * Makes sure any currently selected values are highlighted in the search dialog
	 * 
	 * @param dialog
	 *            the dialog
	 */
	@SuppressWarnings("unchecked")
	public void selectValuesInDialog(ModelBasedSearchDialog<ID, T> dialog) {
		// select any previously selected values in the dialog
		if (multiSelect && getValue() != null && getValue() instanceof Collection) {
			List<ID> ids = new ArrayList<>();
			Collection<T> col = (Collection<T>) getValue();
			for (T t : col) {
				ids.add(t.getId());
			}
			dialog.select(ids);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (selectButton != null) {
			selectButton.setEnabled(enabled);
			clearButton.setEnabled(enabled);
			if (getAddButton() != null) {
				getAddButton().setEnabled(enabled);
			}
		}
	}

	@Override
	protected void setInternalValue(Object newValue) {
		super.setInternalValue(newValue);
		updateLabel(newValue);
	}

	public void setPageLength(Integer pageLength) {
		this.pageLength = pageLength;
	}

	@Override
	public void setValue(Object newFieldValue) {
		super.setValue(newFieldValue);
		updateLabel(newFieldValue);
	}

	/**
	 * Updates the value that is displayed in the label
	 * 
	 * @param newValue
	 *            the new value
	 */
	@SuppressWarnings("unchecked")
	private void updateLabel(Object newValue) {
		if (label != null) {
			label.setCaptionAsHtml(true);

			String caption = getMessageService().getMessage("ocs.no.item.selected");
			if (newValue instanceof Collection<?>) {
				Collection<T> col = (Collection<T>) newValue;
				if (!col.isEmpty()) {
					caption = EntityModelUtil.getDisplayPropertyValue(col, getEntityModel(),
					        SystemPropertyUtils.getLookupFieldMaxItems(), getMessageService());
				}
			} else {
				// just a single value
				T t = (T) newValue;
				if (newValue != null) {
					caption = EntityModelUtil.getDisplayPropertyValue(t, getEntityModel());
				}
			}
			label.setCaption(caption.replaceAll(",", StringUtil.HTML_LINE_BREAK));
		}
	}

}
