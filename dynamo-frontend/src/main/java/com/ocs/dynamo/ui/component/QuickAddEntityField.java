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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.composite.dialog.EntityPopupDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for components that display an Entity or collection of entities
 * and that allow the user to easily add new values on the fly
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 * @param <U>  the type of the value of the component (can be an entity, or a
 *             collection of entities)
 */
public abstract class QuickAddEntityField<ID extends Serializable, T extends AbstractEntity<ID>, U>
		extends CustomEntityField<ID, T, U> implements HasStyle {

	private static final long serialVersionUID = 7118578276952170818L;

	private final EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

	@Getter
	private Button addButton;

	@Getter
	private Button directNavigationButton;

	@Getter
	@Setter
	private SerializablePredicate<T> additionalFilter;

	/**
	 * Constructor
	 * 
	 * @param service        the service used to interact with the storage
	 * @param entityModel    the entity model of the entity that is being displayed
	 * @param attributeModel the attribute model
	 * @param filter         the search filter
	 */
	protected QuickAddEntityField(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			SerializablePredicate<T> filter) {
		super(service, entityModel, attributeModel, filter);
	}

	/**
	 * Method that is called after a new entity has been successfully created. Use
	 * this to add the new entity to the component and select it
	 * 
	 * @param entity the entity
	 */
	protected abstract void afterNewEntityAdded(T entity);

	@Override
	public void clearAdditionalFilter() {
		this.additionalFilter = null;
	}

	/**
	 * Constructs the button that brings up the dialog that allows the user to add a
	 * new item
	 * 
	 * @return the constructed button
	 */
	protected Button constructAddButton() {
		addButton = new Button("");
		addButton.setIcon(VaadinIcon.PLUS.create());
		VaadinUtils.setTooltip(addButton,
				getMessageService().getMessage("ocs.new.value.button", VaadinUtils.getLocale()));
		addButton.addClickListener(event -> {
			EntityModel<T> actualModel = determineEntityModel();
			EntityPopupDialog<ID, T> dialog = new EntityPopupDialog<>(getService(), null, actualModel, null,
					new FormOptions());
			dialog.setAfterEditDone((cancel, isNew, ent) -> {
				if (!cancel) {
					this.afterNewEntityAdded(ent);
				}
			});
			dialog.buildAndOpen();
		});
		return addButton;
	}

	@SuppressWarnings("unchecked")
	private EntityModel<T> determineEntityModel() {
		EntityModel<T> actualModel;
		if (getEntityModel().isBaseEntityModel() || getEntityModel().getReference().contains(".")) {
			actualModel = (EntityModel<T>) entityModelFactory.getModel(getAttributeModel().getNormalizedType());
		} else {
			actualModel = getEntityModel();
		}
		return actualModel;
	}

	/**
	 * Constructs the button that navigates directly to a detail screen for an
	 * entity
	 *
	 * @return the constructed button
	 */
	protected Button constructDirectNavigationButton() {
		directNavigationButton = new Button("");
		directNavigationButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
		VaadinUtils.setTooltip(directNavigationButton, getMessageService().getMessage("ocs.navigate.to",
				VaadinUtils.getLocale(), getEntityModel().getDisplayName(VaadinUtils.getLocale())));
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		directNavigationButton.addClickListener(event -> helper.navigateToEntityScreen(getValue()));
		return directNavigationButton;
	}

	/**
	 * Converts a value (can be a collection but in some cases also a single value)
	 * 
	 * @param value the value to convert
	 * @return the result of the conversion
	 */
	@SuppressWarnings("unchecked")
	protected Object convertToCorrectCollection(Object value) {
		if (value == null) {
			return null;
		} else if (Set.class.isAssignableFrom(getAttributeModel().getType())) {
			Collection<T> col = (Collection<T>) value;
			return new HashSet<>(col);
		} else if (List.class.isAssignableFrom(getAttributeModel().getType())) {
			Collection<T> col = (Collection<T>) value;
			return new ArrayList<>(col);
		} else {
			return value;
		}
	}

	public abstract void setClearButtonVisible(boolean visible);
}
