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
package com.ocs.dynamo.ui.composite.dialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.grid.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A pop-up dialog for adding a new entity or viewing the details of an existing
 * entry
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public class EntityPopupDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

	private static final long serialVersionUID = -2012972894321597214L;

	/**
	 * The entity to add/modify
	 */
	private T entity;

	private EntityModel<T> entityModel;

	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	private FormOptions formOptions;

	private ComponentContext componentContext = ComponentContext.builder().build();

	private SimpleEditLayout<ID, T> layout;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	private List<String> columnThresholds = new ArrayList<>();

	private Button okButton;

	private BaseService<ID, T> service;

	private FetchJoinInformation[] joins;

	@Getter
	@Setter
	private BiConsumer<FlexLayout, Boolean> postProcessButtonBar;

	@Getter
	@Setter
	private Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entity
	 * @param entityModel
	 * @param formOptions
	 */
	public EntityPopupDialog(BaseService<ID, T> service, T entity, EntityModel<T> entityModel,
			Map<String, SerializablePredicate<?>> fieldFilters, FormOptions formOptions,
			FetchJoinInformation... joins) {
		this.service = service;
		this.entityModel = entityModel;
		this.formOptions = formOptions;
		this.entity = entity;
		this.fieldFilters = fieldFilters;
		this.joins = joins;
		setTitle(entityModel.getDisplayName(VaadinUtils.getLocale()));

		setBuildButtonBar(buttonBar -> {
			buttonBar.setVisible(formOptions.isReadOnly());
			if (formOptions.isReadOnly()) {
				okButton = new Button(messageService.getMessage("ocs.ok", VaadinUtils.getLocale()));
				okButton.addClickListener(event -> close());
				buttonBar.add(okButton);
			}
		});
		buildMain();
	}

	private void buildMain() {
		setBuildMain(parent -> {
			// cancel button makes no sense in a popup
			formOptions.setHideCancelButton(false);

			layout = new SimpleEditLayout<ID, T>(entity, service, entityModel, formOptions, componentContext) {

				private static final long serialVersionUID = -2965981316297118264L;

				@Override
				protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
					super.afterEditDone(cancel, newEntity, entity);
					EntityPopupDialog.this.afterEditDone(cancel, newEntity, entity);
					EntityPopupDialog.this.close();
				}

				@Override
				protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
						boolean viewMode, boolean searchMode) {
					return EntityPopupDialog.this.constructCustomField(entityModel, attributeModel, viewMode,
							searchMode);
				}

				@Override
				protected T createEntity() {
					return EntityPopupDialog.this.createEntity();
				}

//				@Override
//				protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
//					EntityPopupDialog.this.postProcessButtonBar(buttonBar, viewMode);
//				}

//				@Override
//				protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
//					EntityPopupDialog.this.postProcessEditFields(editForm);
//				}

			};

			layout.setPostProcessButtonBar(getPostProcessButtonBar());
			layout.setPostProcessEditFields(getPostProcessEditFields());
			layout.setFieldFilters(fieldFilters);
			layout.setColumnThresholds(columnThresholds);
			layout.setJoins(joins);
			parent.add(layout);
		});
	}

	/**
	 * Callback method that is called after the user is done editing the entry
	 * 
	 * @param cancel    whether the edit action was cancelled
	 * @param newEntity whether the user was adding a new entity
	 * @param entity    the entity that was being edited
	 */
	public void afterEditDone(boolean cancel, boolean newEntity, T entity) {
		// override in subclasses
	}

	/**
	 * Creates a new entity
	 * 
	 * @return
	 */
	protected T createEntity() {
		return service.createNewEntity();
	}

//	@Override
//	protected void doBuild(VerticalLayout parent) {
//
//		// cancel button makes no sense in a popup
//		formOptions.setHideCancelButton(false);
//
//		layout = new SimpleEditLayout<ID, T>(entity, service, entityModel, formOptions) {
//
//			private static final long serialVersionUID = -2965981316297118264L;
//
//			@Override
//			protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
//				super.afterEditDone(cancel, newEntity, entity);
//				EntityPopupDialog.this.afterEditDone(cancel, newEntity, entity);
//				EntityPopupDialog.this.close();
//			}
//
//			@Override
//			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
//					boolean viewMode, boolean searchMode) {
//				return EntityPopupDialog.this.constructCustomField(entityModel, attributeModel, viewMode, searchMode);
//			}
//
//			@Override
//			protected T createEntity() {
//				return EntityPopupDialog.this.createEntity();
//			}
//
//			@Override
//			protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
//				EntityPopupDialog.this.postProcessButtonBar(buttonBar, viewMode);
//			}
//
//			@Override
//			protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
//				EntityPopupDialog.this.postProcessEditFields(editForm);
//			}
//
//		};
//		layout.setFieldFilters(fieldFilters);
//		layout.setColumnThresholds(columnThresholds);
//		layout.setJoins(joins);
//		parent.add(layout);
//	}

//	@Override
//	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
//		// in read-only mode, display only an "OK" button that closes the dialog
//		buttonBar.setVisible(formOptions.isReadOnly());
//		if (formOptions.isReadOnly()) {
//			okButton = new Button(messageService.getMessage("ocs.ok", VaadinUtils.getLocale()));
//			okButton.addClickListener(event -> close());
//			buttonBar.add(okButton);
//		}
//	}

	public T getEntity() {
		return layout.getEntity();
	}

	public SimpleEditLayout<ID, T> getLayout() {
		return layout;
	}

	public Button getOkButton() {
		return okButton;
	}

	public List<Button> getSaveButtons() {
		return layout.getEditForm().getSaveButtons();
	}
//
//	protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
//		// overwrite in subclasses when needed
//	}
//
//	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
//		// overwrite in subclasses when needed
//	}

	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, boolean searchMode) {
		// overwrite in subclasses when needed
		return null;
	}

	public List<String> getColumnThresholds() {
		return columnThresholds;
	}

	public void setColumnThresholds(List<String> columnThresholds) {
		this.columnThresholds = columnThresholds;
	}
}
