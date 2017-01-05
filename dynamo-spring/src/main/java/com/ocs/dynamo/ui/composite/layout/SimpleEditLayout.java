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

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout for editing a single entity (can either be an existing or a new entity)
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key of the entity
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public class SimpleEditLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseServiceCustomComponent<ID, T> implements Reloadable, CanAssignEntity<ID, T> {

	private static final long serialVersionUID = -7935358582100755140L;

	// the edit form
	private ModelBasedEditForm<ID, T> editForm;

	// the selected entity
	private T entity;

	// map of additional field filters
	private Map<String, Filter> fieldFilters = new HashMap<>();

	// specifies which relations to fetch when querying
	private FetchJoinInformation[] joins;

	// the main layout
	private VerticalLayout main;

	/**
	 * Constructor
	 * 
	 * @param entity
	 *            the entity to edit
	 * @param service
	 *            the service used to save/refresh the entity
	 * @param entityModel
	 *            the entity model used to generate the form
	 * @param formOptions
	 *            the form options
	 * @param joins
	 *            optional joins to use when fetching the entity from the database
	 */
	public SimpleEditLayout(T entity, BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
	        FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		this.entity = entity;
		this.joins = joins;
	}

	/**
	 * Method that is called after the user has completed (or cancelled) an edit action
	 * 
	 * @param cancel
	 *            whether the edit was cancelled
	 * @param newEntity
	 *            whether a new entity was being edited
	 * @param entity
	 *            the entity that has just been edited
	 */
	protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
		if (entity.getId() != null) {
			// reset to view mode
			if (getFormOptions().isOpenInViewMode()) {
				editForm.setViewMode(true);
			}
			setEntity(getService().fetchById(entity.getId(), getJoins()));
		} else {
			// new entity
			back();
		}
	}

	@Override
	public void assignEntity(T t) {
		setEntity(t);
		if (editForm != null) {
			editForm.resetTab();
		}
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Code to carry out after navigating "back" to the main screen
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
			main = new DefaultVerticalLayout(true, true);

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
				protected void afterModeChanged(boolean viewMode) {
					SimpleEditLayout.this.afterModeChanged(viewMode, editForm);
				}

				@Override
				protected void back() {
					SimpleEditLayout.this.back();
				}

				@Override
				protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
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
				protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
					SimpleEditLayout.this.postProcessButtonBar(buttonBar, viewMode);
				}

				@Override
				protected void postProcessEditFields() {
					SimpleEditLayout.this.postProcessEditFields(editForm);
				}

			};

			editForm.setFieldEntityModels(getFieldEntityModels());
			editForm.build();

			main.addComponent(editForm);

			afterEntitySelected(editForm, getEntity());
			checkButtonState(getEntity());

			setCompositionRoot(main);
		}
	}

	/**
	 * Creates a new entity - override in subclass if needed
	 * 
	 * @return
	 */
	protected T createEntity() {
		return getService().createNewEntity();
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	public T getEntity() {
		return entity;
	}

	public Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	/**
	 * Returns the parent group (which must be returned by the getParentGroupHeaders method) to
	 * which a certain child group belongs
	 * 
	 * @param childGroup
	 *            the name of the child group
	 * @return
	 */
	protected String getParentGroup(String childGroup) {
		// overwrite in subclasses if needed
		return null;
	}

	/**
	 * Returns a list of additional group headers that can be used to add an extra nesting layer to
	 * the layout
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

	/**
	 * Callback method that can be used to add additional buttons to the button bar (at both the top
	 * and the bottom of the screen)
	 * 
	 * @param buttonBar
	 *            the button bar
	 * @param viewMode
	 *            the view mode
	 */
	protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
		// overwrite in subclasses
	}

	/**
	 * @param editForm
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// do nothing by default - override in subclasses
	}

	@Override
	public void reload() {

		// reset to view mode
		if (getFormOptions().isOpenInViewMode()) {
			editForm.setViewMode(true);
		}

		if (entity.getId() != null) {
			setEntity(getService().fetchById(entity.getId(), getJoins()));
			editForm.resetTab();
		}
	}

	/**
	 * Refreshes the contents of a label
	 * 
	 * @param propertyName
	 *            the name of the property for which to refresh the label
	 */
	public void replaceLabel(String propertyName) {
		if (editForm != null) {
			editForm.replaceLabel(propertyName);
		}
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
		checkButtonState(getEntity());
	}

	public void setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setJoins(FetchJoinInformation[] joins) {
		this.joins = joins;
	}

	public void resetTab() {
		editForm.resetTab();
	}

}
