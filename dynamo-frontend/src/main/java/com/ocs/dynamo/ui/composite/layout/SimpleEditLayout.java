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
import java.util.function.BiConsumer;

import com.helger.commons.functional.ITriConsumer;
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
import com.ocs.dynamo.ui.composite.grid.ComponentContext;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

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

	@Getter
	private ModelBasedEditForm<ID, T> editForm;

	@Getter
	private T entity;

	@Getter
	@Setter
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	@Getter
	@Setter
	private BiConsumer<FlexLayout, Boolean> postProcessButtonBar;

	/**
	 * Specifies which relations to fetch. When specified this overrides the default
	 * relations defined in the DAO
	 */
	@Getter
	@Setter
	private FetchJoinInformation[] joins;

	@Getter
	@Setter
	private ITriConsumer<Boolean, Boolean, T> afterEditDone;

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
			ComponentContext context, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions);
		setComponentContext(context);
		setMargin(false);
		setClassName(DynamoConstants.CSS_SIMPLE_EDIT_LAYOUT);
		this.entity = entity;
		this.joins = joins;
	}

	protected final void handleEditDone(boolean cancel, boolean newEntity, T entity) {
		if (entity.getId() != null || getComponentContext().isPopup()) {
			// reset to view mode
			if (getFormOptions().isOpenInViewMode()) {
				editForm.setViewMode(true);
				checkComponentState(getEntity());
			}

			if (afterEditDone != null) {
				afterEditDone.accept(cancel, newEntity, entity);
			}
		} else {
			// new entity
			getOnBackButtonClicked().run();
		}
	}

	@Override
	public void assignEntity(T t) {
		this.entity = t;
		if (editForm != null) {
			editForm.resetTabsheetIfNeeded();
		}
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
				entity = getCreateEntitySupplier().get();
			}

			// there is just one component here, so the screen mode is always
			// vertical
			getFormOptions().setScreenMode(ScreenMode.VERTICAL);
			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), getFormOptions(),
					fieldFilters) {

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
						boolean viewMode) {
					return SimpleEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode, false);
				}

//				@Override
//				protected boolean isEditAllowed() {
//					return SimpleEditLayout.this.isEditAllowed();
//				}

			};

			initEditForm(editForm);
			editForm.setPostProcessButtonBar(postProcessButtonBar);
			editForm.setDetailJoins(getJoins());

			editForm.setAfterEditDone((cancel, isNew, ent) -> {
				setEntity(ent);
				handleEditDone(cancel, isNew, ent);
			});

			editForm.build();

			main.add(editForm);

			postProcessLayout(main);
			add(main);

			if (getComponentContext().getAfterEntitySelected() != null) {
				getComponentContext().getAfterEntitySelected().accept(editForm, getEntity());
			}
			checkComponentState(getEntity());
		}
	}

	public void doSave() {
		this.editForm.doSave();
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

	/**
	 * Sets the entity
	 *
	 * @param entity
	 */
	public void setEntity(T entity) {
		this.entity = entity;
		if (this.entity == null) {
			this.entity = getCreateEntitySupplier().get();
		}
		editForm.setEntity(this.entity);
		if (getComponentContext().getAfterEntitySelected() != null) {
			getComponentContext().getAfterEntitySelected().accept(editForm, entity);
		}

		checkComponentState(getEntity());
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
