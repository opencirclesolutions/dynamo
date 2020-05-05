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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A layout for editing a single entity (can either be an existing or a new
 * entity)
 *
 * @author bas.rutten
 * @param <ID> type of the primary key of the entity
 * @param <T>  type of the entity
 */
@SuppressWarnings("serial")
public class SimpleEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseServiceCustomComponent<ID, T> implements Reloadable, CanAssignEntity<ID, T> {

	private static final long serialVersionUID = -7935358582100755140L;

	private Consumer<T> customSaveConsumer;

	private ModelBasedEditForm<ID, T> editForm;

	private T entity;

	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	/**
	 * Specifies which relations to fetch. When specified this overrides the default
	 * relations defined in the DAO
	 */
	private FetchJoinInformation[] joins;

	private VerticalLayout main;

	/**
	 * Constructor
	 *
	 * @param entity      the entity to edit
	 * @param service     the service used to save/refresh the entity
	 * @param entityModel the entity model used to generate the form
	 * @param formOptions the form options
	 * @param joins       optional joins to use when fetching the entity from the
	 *                    database
	 */
	public SimpleEditLayout(T entity, BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		setMargin(false);
		setClassName(DynamoConstants.CSS_SIMPLE_EDIT_LAYOUT);
		this.entity = entity;
		this.joins = joins;
	}

	/**
	 * Method that is called after the user has completed (or cancelled) an edit
	 * action
	 *
	 * @param cancel    whether the edit was cancelled
	 * @param newEntity whether a new entity was being edited
	 * @param entity    the entity that has just been edited
	 */
	protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
		if (entity.getId() != null) {
			// reset to view mode
			if (getFormOptions().isOpenInViewMode()) {
				editForm.setViewMode(true);
				checkComponentState(getEntity());
			}
		} else {
			// new entity
			back();
		}
	}

	/**
	 * Callback method that fires directly after the entity is set and before any
	 * data binding occurs
	 * 
	 * @param entity the entity that was just set
	 */
	public void afterEntitySet(T entity) {
		// overwrite in entity
	}

	/**
	 * Callback method that fires after one of the layouts of the underlying edit
	 * form is built for the first time
	 *
	 * @param layout   the layout
	 * @param viewMode whether the layout is in view mode
	 */
	protected void afterLayoutBuilt(HasComponents layout, boolean viewMode) {
		// override in subclass
	}

	/**
	 * Callback method that first after a tab has been selected in the tab sheet
	 * that is used in a detail form when the attribute group mode has been set to
	 * TABSHEET
	 *
	 * @param tabIndex the zero-based index of the selected tab
	 */
	protected void afterTabSelected(int tabIndex) {
		// overwrite in subclasses
	}

	@Override
	public void assignEntity(T t) {
		this.entity = t;
		if (editForm != null) {
			editForm.resetTabsheetIfNeeded();
		}
	}

	/**
	 * Callback method that fires after the "back" button is pressed
	 */
	protected void back() {
		// overwrite in subclasses
	}

	/**
	 * Constructs the screen - this method is called just once
	 */
	@Override
	public void build() {
		if (main == null) {
			main = new DefaultVerticalLayout();

			// create new entity if it does not exist yet
			if (entity == null) {
				entity = createEntity();
			}

			// there is just one component here, so the screen mode is always
			// vertical
			getFormOptions().setScreenMode(ScreenMode.VERTICAL);
			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), getFormOptions(),
					fieldFilters) {

				@Override
				protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
					setEntity(entity);
					SimpleEditLayout.this.afterEditDone(cancel, newObject, entity);
				}

				@Override
				protected void afterEntitySet(T entity) {
					SimpleEditLayout.this.afterEntitySet(entity);
				}

				@Override
				protected void afterLayoutBuilt(HasComponents layout, boolean viewMode) {
					SimpleEditLayout.this.afterLayoutBuilt(layout, viewMode);
				}

				@Override
				protected void afterModeChanged(boolean viewMode) {
					SimpleEditLayout.this.afterModeChanged(viewMode, editForm);
				}

				@Override
				protected void afterTabSelected(int tabIndex) {
					SimpleEditLayout.this.afterTabSelected(tabIndex);
				}

				@Override
				protected void back() {
					SimpleEditLayout.this.back();
				}

				@Override
				protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
					return SimpleEditLayout.this.constructCustomConverter(am);
				}

				@Override
				protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
					return SimpleEditLayout.this.constructCustomValidator(am);
				}

				@Override
				protected <V> Validator<V> constructCustomRequiredValidator(AttributeModel am) {
					return SimpleEditLayout.this.constructCustomRequiredValidator(am);
				}

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
						boolean viewMode) {
					return SimpleEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode, false);
				}

				@Override
				protected String getParentGroup(String childGroup) {
					return SimpleEditLayout.this.getParentGroup(childGroup);
				}

				@Override
				protected String[] getParentGroupHeaders() {
					return SimpleEditLayout.this.getParentGroupHeaders();
				}

				@Override
				protected boolean isEditAllowed() {
					return SimpleEditLayout.this.isEditAllowed();
				}

				@Override
				protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
					SimpleEditLayout.this.postProcessButtonBar(buttonBar, viewMode);
				}

				@Override
				protected void postProcessEditFields() {
					SimpleEditLayout.this.postProcessEditFields(editForm);
				}
			};

			editForm.setCustomSaveConsumer(customSaveConsumer);
			editForm.setDetailJoins(getJoins());
			editForm.setFieldEntityModels(getFieldEntityModels());
			editForm.build();

			main.add(editForm);

			postProcessLayout(main);
			add(main);
			afterEntitySelected(editForm, getEntity());
			checkComponentState(getEntity());
		}
	}

	/**
	 * Callback method that fires when the new entity is being created
	 *
	 * @return
	 */
	protected T createEntity() {
		return getService().createNewEntity();
	}

	public void doSave() {
		this.editForm.doSave();
	}

	public Consumer<T> getCustomSaveConsumer() {
		return customSaveConsumer;
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	public T getEntity() {
		return entity;
	}

	public Map<String, SerializablePredicate<?>> getFieldFilters() {
		return fieldFilters;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	/**
	 * Returns the parent group (which must be returned by the getParentGroupHeaders
	 * method) to which a certain child group belongs
	 *
	 * @param childGroup the name of the child group
	 * @return
	 */
	protected String getParentGroup(String childGroup) {
		// overwrite in subclasses if needed
		return null;
	}

	/**
	 * Returns a list of additional group headers that can be used to add an extra
	 * nesting layer to the layout
	 *
	 * @return
	 */
	protected String[] getParentGroupHeaders() {
		// overwrite in subclasses if needed
		return null;
	}

	/**
	 *
	 * @return
	 */
	protected boolean isEditAllowed() {
		return true;
	}

	/**
	 * Check if the layout is in edit mode
	 *
	 * @return
	 */
	public boolean isViewMode() {
		return editForm.isViewMode();
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	/**
	 * Callback method that can be used to add additional buttons to the button bar
	 * (at both the top and the bottom of the screen)
	 *
	 * @param buttonBar the button bar
	 * @param viewMode  the view mode
	 */
	protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
		// overwrite in subclasses
	}

	/**
	 * Callback method that is called after the edit form has been constructed
	 * 
	 * @param editForm the edit form
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// do nothing by default - override in subclasses
	}

	/**
	 * Method that is called after the entire layout has been constructed. Use this
	 * to e.g. add additional components to the bottom of the layout
	 *
	 * @param main the main layout
	 */
	protected void postProcessLayout(VerticalLayout main) {
		// overwrite in subclass
	}

	@Override
	public void reload() {

		// reset to view mode
		if (getFormOptions().isOpenInViewMode()) {
			editForm.setViewMode(true);
		}

		if (entity.getId() != null) {
			setEntity(getService().fetchById(entity.getId(), getJoins()));
			editForm.resetTabsheetIfNeeded();
		}
	}

	/**
	 * Resets the tab component (if any) to its first sheet
	 */
	public void resetTab() {
		editForm.resetTabsheetIfNeeded();
	}

	public void selectTab(int index) {
		editForm.selectTab(index);
	}

	public void setCustomSaveConsumer(Consumer<T> customSaveConsumer) {
		this.customSaveConsumer = customSaveConsumer;
	}

	/**
	 * Sets the entity
	 *
	 * @param entity
	 */
	public void setEntity(T entity) {
		this.entity = entity;
		if (this.entity == null) {
			this.entity = createEntity();
		}
		editForm.setEntity(this.entity);
		afterEntitySelected(editForm, this.entity);
		checkComponentState(getEntity());
	}

	public void setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setJoins(FetchJoinInformation[] joins) {
		this.joins = joins;
	}

	/**
	 * Replaces the contents of a label by its current value. Use in response to an
	 * automatic update if a field
	 *
	 * @param path  the path of the property
	 * @param value the name
	 */
	public void setLabelValue(String path, String value) {
		if (editForm != null) {
			editForm.setLabelValue(path, value);
		}
	}

}
