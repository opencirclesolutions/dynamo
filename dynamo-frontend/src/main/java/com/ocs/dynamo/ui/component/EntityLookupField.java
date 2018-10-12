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
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.utils.EntityModelUtil;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.StringUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import org.apache.commons.lang.ArrayUtils;

/**
 * A composite component that displays a selected entity and offers a search
 * dialog to search for another one
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class EntityLookupField<ID extends Serializable, T extends AbstractEntity<ID>>
		extends QuickAddEntityField<ID, T, Object> {

	private static final long serialVersionUID = 5377765863515463622L;

	private boolean directNavigationAllowed;

	/**
	 * Indicates whether it is allowed to clear the selection
	 */
	private boolean clearAllowed;

	/**
	 * Indicates whether it is allowed to add items
	 */
	private boolean addAllowed;

	/**
	 * The button used to clear the current selection
	 */
	private Button clearButton;

	/**
	 * The joins to apply to the search in the search dialog
	 */
	private FetchJoinInformation[] joins;

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
	 * @param filter
	 *            the filter to apply when searching
	 * @param search
	 *            whether the component is used in a search screen
	 * @param sortOrders
	 *            the sort order
	 * @param joins
	 *            the joins to use when fetching data when filling the popop dialog
	 */
	public EntityLookupField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			Filter filter, boolean search, boolean multiSelect, List<SortOrder> sortOrders,
			FetchJoinInformation... joins) {
		super(service, entityModel, attributeModel, filter);
		this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
		this.joins = joins;
		this.multiSelect = multiSelect;
		this.clearAllowed = true;
		this.addAllowed = !search && (attributeModel != null && attributeModel.isQuickAddAllowed());
		this.directNavigationAllowed = !search && (attributeModel != null && attributeModel.isDirectNavigation());
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
	public Class<?> getType() {
		return Object.class;
	}

	protected boolean isDirectNavigationAllowed() {
		return directNavigationAllowed;
	}

	protected void setDirectNavigationAllowed(boolean directNavigationAllowed) {
		this.directNavigationAllowed = directNavigationAllowed;
	}

	protected boolean isClearAllowed() {
		return clearAllowed;
	}

	protected void setClearAllowed(boolean clearAllowed) {
		this.clearAllowed = clearAllowed;
	}

	protected boolean isAddAllowed() {
		return addAllowed;
	}

	protected void setAddAllowed(boolean addAllowed) {
		this.addAllowed = addAllowed;
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
		bar.addComponent(VaadinUtils.wrapInFormLayout(label));

		// button for selecting an entity - brings up the search dialog
		selectButton = new Button(getMessageService().getMessage("ocs.select", VaadinUtils.getLocale()));
		selectButton.setIcon(FontAwesome.SEARCH);
		selectButton.addClickListener(event -> {
			List<Filter> filterList = new ArrayList<>();
			if (getFilter() != null) {
				filterList.add(getFilter());
			}
			if (getAdditionalFilter() != null) {
				filterList.add(getAdditionalFilter());
			}

			ModelBasedSearchDialog<ID, T> dialog = new ModelBasedSearchDialog<ID, T>(getService(), getEntityModel(),
					filterList, sortOrders, multiSelect, true, getJoins()) {

				private static final long serialVersionUID = -3432107069929941520L;

                @Override

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
        });
        bar.addComponent(selectButton);

		// button for clearing the current selection
		if (clearAllowed) {
			clearButton = new Button(getMessageService().getMessage("ocs.clear", VaadinUtils.getLocale()));
			clearButton.setIcon(FontAwesome.ERASER);
			clearButton.addClickListener(event -> setValue(null));
			bar.addComponent(clearButton);
		}

		// quick add button
		if (addAllowed) {
			Button addButton = constructAddButton();
			bar.addComponent(addButton);
		}
		if (directNavigationAllowed) {
			Button directNavigationButton = constructDirectNavigationButton();
			bar.addComponent(directNavigationButton);
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
			if (getClearButton() != null) {
				getClearButton().setEnabled(enabled);
			}
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
	private void updateLabel(Object newValue) {
		if (label != null) {
			label.setCaptionAsHtml(true);
			String caption = getLabel(newValue);
			label.setCaption(caption.replaceAll(",", StringUtils.HTML_LINE_BREAK));
		}
	}

	@SuppressWarnings("unchecked")
	protected String getLabel(Object newValue) {
		String caption = getMessageService().getMessage("ocs.no.item.selected", VaadinUtils.getLocale());
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
		return caption;
	}

	@Override
	public void refresh(Filter filter) {
		setFilter(filter);
	}

	@Override
	public void setAdditionalFilter(Filter additionalFilter) {
		setValue(null);
		super.setAdditionalFilter(additionalFilter);
	}

	protected FetchJoinInformation[] getJoins() {
		return joins;
	}

	public void addFetchJoinInformation(FetchJoinInformation... fetchJoinInformation) {
		joins = (FetchJoinInformation[]) ArrayUtils.addAll(joins, fetchJoinInformation);
	}

}
