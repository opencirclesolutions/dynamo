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
package com.ocs.dynamo.ui.composite.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.CollapsiblePanel;
import com.ocs.dynamo.ui.component.DefaultEmbedded;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.QuickAddListSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.utils.EntityModelUtil;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

/**
 * An edit form that is constructed based on an entity model
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
@SuppressWarnings("serial")
public class ModelBasedEditForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelBasedForm<ID, T> {

	/**
	 * A custom field that can be used to upload a file
	 * 
	 * @author bas.rutten
	 *
	 */
	private final class UploadComponent extends CustomField<byte[]> {

		private AttributeModel attributeModel;

		private UploadComponent(AttributeModel attributeModel) {
			this.attributeModel = attributeModel;
		}

		@Override
		public Class<? extends byte[]> getType() {
			return byte[].class;
		}

		@Override
		protected Component initContent() {
			final byte[] bytes = ClassUtils.getBytes(getEntity(), attributeModel.getName());
			final Embedded image = new DefaultEmbedded(null, bytes);

			VerticalLayout main = new DefaultVerticalLayout(false, true);

			// for a LOB field, create an upload and an image
			// retrieve the current value
			if (attributeModel.isImage()) {
				image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
				image.setVisible(bytes != null);
				main.addComponent(image);
			} else {
				Label label = new Label(message("ocs.no.preview.available"));
				main.addComponent(label);
			}

			// callback object to handle successful upload
			UploadReceiver receiver = new UploadReceiver(image, attributeModel.getName(),
					attributeModel.getFileNameProperty(), attributeModel.getAllowedExtensions().toArray(new String[0]));

			HorizontalLayout buttonBar = new DefaultHorizontalLayout(false, true, true);
			main.addComponent(buttonBar);

			Upload upload = new Upload(null, receiver);
			upload.addSucceededListener(receiver);
			buttonBar.addComponent(upload);

			// a button used to clear the image
			Button clearButton = new Button(message("ocs.clear"));
			clearButton.addClickListener(event -> {
				ClassUtils.clearFieldValue(getEntity(), attributeModel.getName(), byte[].class);
				image.setVisible(false);
				if (attributeModel.getFileNameProperty() != null) {
					ClassUtils.clearFieldValue(getEntity(), attributeModel.getFileNameProperty(), String.class);
					refreshLabel(attributeModel.getFileNameProperty());
				}
			});
			buttonBar.addComponent(clearButton);
			setCaption(attributeModel.getDisplayName());

			return main;
		}

	}

	/**
	 * Callback object for handling a file upload
	 * 
	 * @author bas.rutten
	 */
	private final class UploadReceiver implements SucceededListener, Receiver {

		// the name of the field that must be updated
		private String fieldName;

		// the name of the file that is uploaded
		private String fileNameFieldName;

		private ByteArrayOutputStream stream;

		private String[] supportedExtensions;

		// the target component that must be updated after an upload
		private Embedded target;

		/**
		 * Constructor
		 * 
		 * @param target
		 *            the target component that must be updated after an upload
		 * @param fieldName
		 *            the name of the field
		 * @param supportedExtensions
		 *            the supported file extensions
		 */
		private UploadReceiver(Embedded target, String fieldName, String fileNameFieldName,
				String... supportedExtensions) {
			this.target = target;
			this.fieldName = fieldName;
			this.fileNameFieldName = fileNameFieldName;
			this.supportedExtensions = supportedExtensions;
		}

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			stream = new ByteArrayOutputStream();
			return stream;
		}

		@Override
		public void uploadSucceeded(SucceededEvent event) {
			if (stream != null && stream.toByteArray().length > 0) {
				String extension = FilenameUtils.getExtension(event.getFilename());

				if (supportedExtensions == null || supportedExtensions.length == 0
						|| (extension != null && Arrays.asList(supportedExtensions).contains(extension))) {

					// set the image source
					if (target != null) {
						target.setVisible(true);
						StreamResource.StreamSource ss = (StreamResource.StreamSource) () -> new ByteArrayInputStream(
								stream.toByteArray());
						target.setSource(new StreamResource(ss, System.nanoTime() + ".png"));
					}

					// copy the bytes to the entity
					ClassUtils.setBytes(stream.toByteArray(), getEntity(), fieldName);

					// also set the file name if needed
					if (fileNameFieldName != null) {
						ClassUtils.setFieldValue(getEntity(), fileNameFieldName, event.getFilename());
						refreshLabel(fileNameFieldName);
					}
				} else {
					showNotifification(message("ocs.modelbasededitform.upload.format.invalid"),
							Notification.Type.ERROR_MESSAGE);
				}
			}
		}
	}

	private static final long serialVersionUID = 2201140375797069148L;

	private static final String EDIT_BUTTON_DATA = "editButton";

	private static final String SAVE_BUTTON_DATA = "saveButton";

	private static final String BACK_BUTTON_DATA = "backButton";

	private static final String CANCEL_BUTTON_DATA = "cancelButton";

	private static final String NEXT_BUTTON_DATA = "nextButton";

	private static final String PREV_BUTTON_DATA = "prevButton";

	/**
	 * For keeping track of attribute groups per view mode
	 */
	private Map<Boolean, Map<String, Object>> attributeGroups = new HashMap<>();

	/**
	 * The relations to fetch when selecting a single detail relation
	 */
	private FetchJoinInformation[] detailJoins;

	/**
	 * Indicates whether all details tables for editing complex fields are valid
	 */
	private Map<SignalsParent, Boolean> detailTablesValid = new HashMap<>();

	/**
	 * The selected entity
	 */
	private T entity;

	/**
	 * The field factory
	 */
	private ModelBasedFieldFactory<T> fieldFactory;

	/**
	 * Indicates whether the fields have been post processed
	 */
	private boolean fieldsProcessed;

	/**
	 * Groups for data binding (one for each view mode)
	 */
	private Map<Boolean, BeanFieldGroup<T>> groups = new HashMap<>();

	/**
	 * A map containing all the labels that were added - used to replace the label
	 * values as the selected entity changes
	 */
	private Map<Boolean, Map<AttributeModel, Component>> labels = new HashMap<>();

	private VerticalLayout mainEditLayout;

	private VerticalLayout mainViewLayout;

	private Map<Boolean, List<Button>> buttons = new HashMap<>();

	private BaseService<ID, T> service;

	private Map<Boolean, TabSheet> tabSheets = new HashMap<>();

	private Map<Boolean, HorizontalLayout> titleBars = new HashMap<>();

	private Map<Boolean, Label> titleLabels = new HashMap<>();

	private Map<Boolean, Map<AttributeModel, Component>> uploads = new HashMap<>();

	private Map<Boolean, Map<AttributeModel, Component>> previews = new HashMap<>();

	private Map<Boolean, Set<String>> alreadyBound = new HashMap<>();

	private Map<Integer, Field<?>> firstFields = new HashMap<>();

	/**
	 * The width of the title caption above the form (in pixels)
	 */
	private Integer formTitleWidth;

	/**
	 * Whether the component supports iteration over the records
	 */
	private boolean supportsIteration;

	/**
	 * Whether to display the component in view mode
	 */
	private boolean viewMode;

	/**
	 * Whether the form is in nested mode
	 */
	private boolean nestedMode;

	/**
	 * Constructor
	 * 
	 * @param entity
	 *            the entity
	 * @param service
	 *            the service
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 * @param fieldFilters
	 *            the field filters
	 */
	public ModelBasedEditForm(T entity, BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			Map<String, Filter> fieldFilters) {
		super(formOptions, fieldFilters, entityModel);
		this.service = service;
		this.entity = entity;
		afterEntitySet(entity);
		Class<T> clazz = service.getEntityClass();

		// set the custom field factory
		this.fieldFactory = ModelBasedFieldFactory.getInstance(entityModel, getMessageService());

		// open in view mode when this is requested, and it is not a new object
		this.viewMode = !isEditAllowed() || (formOptions.isOpenInViewMode() && entity.getId() != null);

		// set up a bean field group for automatic binding and validation
		BeanItem<T> beanItem = new BeanItem<>(entity);
		BeanFieldGroup<T> group = new BeanFieldGroup<>(clazz);
		group.setItemDataSource(beanItem);
		group.setBuffered(false);
		groups.put(Boolean.FALSE, group);

		beanItem = new BeanItem<>(entity);
		group = new BeanFieldGroup<>(clazz);
		group.setItemDataSource(beanItem);
		group.setBuffered(false);
		groups.put(Boolean.TRUE, group);

		// init panel maps
		attributeGroups.put(Boolean.TRUE, new HashMap<>());
		attributeGroups.put(Boolean.FALSE, new HashMap<>());

		alreadyBound.put(Boolean.TRUE, new HashSet<>());
		alreadyBound.put(Boolean.FALSE, new HashSet<>());

		buttons.put(Boolean.TRUE, new ArrayList<>());
		buttons.put(Boolean.FALSE, new ArrayList<>());
	}

	/**
	 * Adds a field for a certain attribute
	 * 
	 * @param parent
	 *            the layout to which to add the field
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 */
	private void addField(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel, int tabIndex) {
		AttributeType type = attributeModel.getAttributeType();
		if (!alreadyBound.get(isViewMode()).contains(attributeModel.getPath()) && attributeModel.isVisible()
				&& (AttributeType.BASIC.equals(type) || AttributeType.LOB.equals(type)
						|| attributeModel.isComplexEditable())) {

			if (EditableType.READ_ONLY.equals(attributeModel.getEditableType()) || isViewMode()) {
				if (attributeModel.isUrl()) {
					// display a complex component even in read-only mode
					constructField(parent, entityModel, attributeModel, true, tabIndex);
				} else if (AttributeType.LOB.equals(type) && attributeModel.isImage()) {
					// image preview
					Component c = constructImagePreview(attributeModel);
					parent.addComponent(c);
					previews.get(isViewMode()).put(attributeModel, c);
				} else if (AttributeType.DETAIL.equals(type) && attributeModel.isComplexEditable()) {
					Field<?> f = constructCustomField(entityModel, attributeModel, viewMode);
					if (f instanceof DetailsEditTable || f instanceof DetailsEditLayout) {
						// a details edit table or details edit layout must always be displayed
						constructField(parent, entityModel, attributeModel, true, tabIndex);
					} else {
						constructLabel(parent, entityModel, attributeModel, tabIndex);
					}
				} else {
					// otherwise display a label
					constructLabel(parent, entityModel, attributeModel, tabIndex);
				}
			} else {
				// display an editable field
				if (AttributeType.BASIC.equals(type) || AttributeType.MASTER.equals(type)
						|| AttributeType.DETAIL.equals(type) || AttributeType.ELEMENT_COLLECTION.equals(type)) {
					constructField(parent, entityModel, attributeModel, false, tabIndex);
				} else if (AttributeType.LOB.equals(type)) {
					// for a LOB field we need to construct a rather
					// elaborate upload component
					UploadComponent uploadForm = constructUploadField(attributeModel);
					parent.addComponent(uploadForm);
					uploads.get(isViewMode()).put(attributeModel, uploadForm);
				}
			}
			alreadyBound.get(isViewMode()).add(attributeModel.getPath());
		}
	}

	/**
	 * Add a listener to respond to a tab change and focus the first available field
	 * 
	 * @param tabSheet
	 */
	private void addTabChangeListener(TabSheet tabSheet) {
		tabSheet.addSelectedTabChangeListener(event -> {
			Component c = event.getTabSheet().getSelectedTab();
			if (tabSheets.get(isViewMode()) != null && tabSheets.get(isViewMode()).getTab(c) != null) {
				int index = VaadinUtils.getTabIndex(tabSheets.get(isViewMode()),
						tabSheets.get(isViewMode()).getTab(c).getCaption());
				if (firstFields.get(index) != null) {
					firstFields.get(index).focus();
				}
			}
		});
	}

	/**
	 * Method that is called after the user is done editing an entity
	 * 
	 * @param cancel
	 *            whether the user cancelled the editing
	 * @param newObject
	 *            whether the object is a new object
	 * @param entity
	 *            the entity
	 */
	protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
		// override in subclass
	}

	/**
	 * Respond to the setting of a new entity as the selected entity. This can be
	 * used to fetch any additionally required data
	 * 
	 * @param entity
	 *            the entity
	 */
	protected void afterEntitySet(T entity) {
		// override in subclass
	}

	/**
	 * 
	 * @param layout
	 * @param viewMode
	 */
	protected void afterLayoutBuilt(Layout layout, boolean viewMode) {
		// after the layout
	}

	/**
	 * Callback method that is called after the mode has changed from or to view
	 * mode
	 */
	protected void afterModeChanged(boolean viewMode) {
		// overwrite in subclasses
	}

	/**
	 * Called after the user navigates back to a search screen using the back button
	 * 
	 * @return
	 */
	protected void back() {
		// overwrite in subclasses
	}

	/**
	 * Main build method - lazily constructs the layout for either edit or view mode
	 */
	@Override
	public void build() {
		if (isViewMode()) {
			if (mainViewLayout == null) {
				Map<AttributeModel, Component> map = new HashMap<>();
				labels.put(Boolean.TRUE, map);

				Map<AttributeModel, Component> uploadMap = new HashMap<>();
				uploads.put(Boolean.TRUE, uploadMap);

				Map<AttributeModel, Component> previewMap = new HashMap<>();
				previews.put(Boolean.TRUE, previewMap);

				mainViewLayout = buildMainLayout(getEntityModel());
			}
			setCompositionRoot(mainViewLayout);
		} else {
			if (mainEditLayout == null) {
				Map<AttributeModel, Component> map = new HashMap<>();
				labels.put(Boolean.FALSE, map);

				Map<AttributeModel, Component> uploadMap = new HashMap<>();
				uploads.put(Boolean.FALSE, uploadMap);

				Map<AttributeModel, Component> previewMap = new HashMap<>();
				previews.put(Boolean.FALSE, previewMap);

				mainEditLayout = buildMainLayout(getEntityModel());

				if (!fieldsProcessed) {
					postProcessEditFields();
					fieldsProcessed = true;
				}
			}
			setCompositionRoot(mainEditLayout);
		}

	}

	/**
	 * Constructs the main layout of the screen
	 * 
	 * @param entityModel
	 * @return
	 */
	protected VerticalLayout buildMainLayout(EntityModel<T> entityModel) {
		VerticalLayout layout = new DefaultVerticalLayout(false, true);

		titleLabels.put(isViewMode(), constructTitleLabel());
		titleBars.put(isViewMode(), new DefaultHorizontalLayout(false, true, true));
		titleBars.get(isViewMode()).addComponent(titleLabels.get(isViewMode()));

		HorizontalLayout buttonBar = null;
		if (!nestedMode) {
			buttonBar = constructButtonBar();
			buttonBar.setSizeUndefined();
			if (getFormOptions().isPlaceButtonBarAtTop()) {
				layout.addComponent(buttonBar);
			} else {
				titleBars.get(isViewMode()).addComponent(buttonBar);
			}
		}
		layout.addComponent(titleBars.get(isViewMode()));
		titleBars.get(isViewMode()).setVisible(!nestedMode);

		Layout form = null;
		if (entityModel.usesDefaultGroupOnly()) {
			form = new FormLayout();
		} else {
			form = new DefaultVerticalLayout(false, true);
		}

		// in case of vertical layout (the default), don't use the entire screen
		if (ScreenMode.VERTICAL.equals(getFormOptions().getScreenMode())) {
			form.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
		}

		if (!entityModel.usesDefaultGroupOnly()) {
			// display the attributes in groups

			boolean tabs = AttributeGroupMode.TABSHEET.equals(getFormOptions().getAttributeGroupMode());
			if (tabs) {
				TabSheet tabSheet = new TabSheet();
				tabSheets.put(isViewMode(), tabSheet);
				form.addComponent(tabSheet);

				// focus first field after tab change
				addTabChangeListener(tabSheet);
			}

			if (getParentGroupHeaders() != null && getParentGroupHeaders().length > 0) {
				// extra layer of grouping (always tabs)
				int tabIndex = 0;
				for (String parentGroupHeader : getParentGroupHeaders()) {
					Layout innerForm = constructAttributeGroupLayout(form, tabs, tabSheets.get(isViewMode()),
							parentGroupHeader, false);

					// add a tab sheet on the inner level if needed
					TabSheet innerTabSheet = null;
					boolean innerTabs = !tabs;
					if (innerTabs) {
						innerTabSheet = new TabSheet();
						innerForm.addComponent(innerTabSheet);
					}

					// add all appropriate inner groups
					processParentHeaderGroup(parentGroupHeader, innerForm, innerTabs, innerTabSheet, tabIndex);
					tabIndex++;
				}
			} else {
				// just one layer of attribute groups
				int tabIndex = 0;
				for (String attributeGroup : entityModel.getAttributeGroups()) {
					if (entityModel.isAttributeGroupVisible(attributeGroup, isViewMode())) {
						Layout innerForm = constructAttributeGroupLayout(form, tabs, tabSheets.get(isViewMode()),
								attributeGroup, true);
						if (ScreenMode.VERTICAL.equals(getFormOptions().getScreenMode())) {
							innerForm.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
						}

						for (AttributeModel attributeModel : entityModel.getAttributeModelsForGroup(attributeGroup)) {
							addField(innerForm, entityModel, attributeModel, tabIndex);
						}
						if (AttributeGroupMode.TABSHEET.equals(getFormOptions().getAttributeGroupMode())) {
							tabIndex++;
						}
					}
				}
			}
		} else {
			// iterate over the attributes and add them to the form (without any
			// grouping)
			for (AttributeModel attributeModel : entityModel.getAttributeModels()) {
				addField(form, entityModel, attributeModel, 0);
			}
		}

		constructCascadeListeners();
		layout.addComponent(form);

		if (firstFields.get(0) != null) {
			firstFields.get(0).focus();
		}

		if (!nestedMode) {
			buttonBar = constructButtonBar();
			buttonBar.setSizeUndefined();
			layout.addComponent(buttonBar);
		}
		checkSaveButtonState();
		disableCreateOnlyFields();

		afterLayoutBuilt(form, isViewMode());

		return layout;
	}

	/**
	 * Checks the state of the iteration (prev/next) buttons. These will be shown in
	 * view mode or if the form only supports an edit mode
	 * 
	 * @param checkEnabled
	 *            whether to check if the buttons should be enabled
	 */
	private void checkIterationButtonState(boolean checkEnabled) {
		for (Button b : filterButtons(NEXT_BUTTON_DATA)) {
			b.setVisible(isSupportsIteration() && getFormOptions().isShowNextButton() && entity.getId() != null);
			if (checkEnabled && b.isVisible() && (isViewMode() || !getFormOptions().isOpenInViewMode())) {
				b.setEnabled(true);
			} else {
				b.setEnabled(false);
			}
		}
		for (Button b : filterButtons(PREV_BUTTON_DATA)) {
			b.setVisible(isSupportsIteration() && getFormOptions().isShowPrevButton() && entity.getId() != null);
			if (checkEnabled && b.isVisible() && (isViewMode() || !getFormOptions().isOpenInViewMode())) {
				b.setEnabled(true);
			} else {
				b.setEnabled(false);
			}
		}
	}

	/**
	 * Sets the state (enabled/disabled) of the save button. The button is enabled
	 * if the from is valid and all of its detail tables are as well
	 */
	private void checkSaveButtonState() {
		for (Button saveButton : buttons.get(isViewMode())) {
			if (SAVE_BUTTON_DATA.equals(saveButton.getData())) {
				saveButton.setEnabled(isValid());
			}
		}
	}

	/**
	 * Check if the form is valid
	 * 
	 * @return
	 */
	public boolean isValid() {
		boolean valid = groups.get(isViewMode()).isValid();
		valid &= detailTablesValid.values().stream().allMatch(x -> x);
		return valid;
	}

	/**
	 * Construct the layout (form and panel) for an attribute group
	 * 
	 * @param parent
	 *            the parent component
	 * @param tabs
	 *            whether to include the component in a tab sheet
	 * @param tabSheet
	 *            the parent tab sheet (only used if the "tabs" parameter is true)
	 * @param caption
	 *            caption of the panel or tab sheet
	 * @param lowest
	 *            indicates whether this is the lowest level
	 * @return
	 */
	private Layout constructAttributeGroupLayout(Layout parent, boolean tabs, TabSheet tabSheet, String messageKey,
			boolean lowest) {
		Layout innerLayout = null;
		if (lowest) {
			innerLayout = new FormLayout();
			((FormLayout) innerLayout).setMargin(true);
			if (!tabs) {
				innerLayout.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);
			}
		} else {
			innerLayout = new DefaultVerticalLayout(true, true);
		}

		if (tabs) {
			Tab added = tabSheet.addTab(innerLayout, message(messageKey));
			attributeGroups.get(isViewMode()).put(messageKey, added);
		} else {
			CollapsiblePanel panel = new CollapsiblePanel();
			panel.setStyleName("attributePanel");
			panel.setCaption(message(messageKey));
			panel.setContent(innerLayout);
			parent.addComponent(panel);

			attributeGroups.get(isViewMode()).put(messageKey, panel);
		}
		return innerLayout;
	}

	private HorizontalLayout constructButtonBar() {
		HorizontalLayout buttonBar = new DefaultHorizontalLayout();

		// button to go back to the main screen when in view mode
		Button backButton = new Button(message("ocs.back"));
		backButton.addClickListener(event -> back());
		backButton.setVisible(isViewMode() && getFormOptions().isShowBackButton());
		backButton.setData(BACK_BUTTON_DATA);
		buttonBar.addComponent(backButton);
		buttons.get(isViewMode()).add(backButton);

		// in edit mode, display a cancel button
		Button cancelButton = new Button(message("ocs.cancel"));
		cancelButton.addClickListener(event -> {
			if (entity.getId() != null) {
				entity = service.fetchById(entity.getId(), getDetailJoins());
			}
			afterEditDone(true, entity.getId() == null, entity);
		});
		cancelButton.setVisible((!isViewMode() && !getFormOptions().isHideCancelButton())
				|| (getFormOptions().isFormNested() && entity.getId() == null));
		cancelButton.setData(CANCEL_BUTTON_DATA);
		buttonBar.addComponent(cancelButton);
		buttons.get(isViewMode()).add(cancelButton);

		// create the save button
		if (!isViewMode()) {
			Button saveButton = constructSaveButton();
			buttonBar.addComponent(saveButton);
			buttons.get(isViewMode()).add(saveButton);

		}

		// create the edit button
		Button editButton = new Button(message("ocs.edit"));
		editButton.addClickListener(event -> setViewMode(false));
		buttonBar.addComponent(editButton);
		buttons.get(isViewMode()).add(editButton);
		editButton.setData(EDIT_BUTTON_DATA);
		editButton.setVisible(isViewMode() && getFormOptions().isEditAllowed() && isEditAllowed());

		// button for moving to the previous record
		Button prevButton = new Button(message("ocs.previous"));
		prevButton.addClickListener(e -> {
			T prev = getPrevEntity(getEntity());
			if (prev != null) {
				setEntity(prev, true);
			} else {
				prevButton.setEnabled(false);
			}
		});
		prevButton.setData(PREV_BUTTON_DATA);
		buttons.get(isViewMode()).add(prevButton);
		buttonBar.addComponent(prevButton);

		// button for moving to the next record
		Button nextButton = new Button(message("ocs.next"));
		nextButton.addClickListener(e -> {
			T next = getNextEntity(getEntity());
			if (next != null) {
				setEntity(next, true);
			} else {
				nextButton.setEnabled(false);
			}
		});
		nextButton.setData(NEXT_BUTTON_DATA);
		buttons.get(isViewMode()).add(nextButton);

		buttonBar.addComponent(nextButton);
		prevButton.setVisible(isSupportsIteration() && getFormOptions().isShowPrevButton() && entity.getId() != null);
		nextButton.setVisible(isSupportsIteration() && getFormOptions().isShowNextButton() && entity.getId() != null);

		postProcessButtonBar(buttonBar, isViewMode());

		return buttonBar;
	}

	/**
	 * Adds any value change listeners for taking care of cascading search
	 */
	private void constructCascadeListeners() {
		for (final AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
			Field<?> field = groups.get(isViewMode()).getField(am.getPath());
			if (field != null) {
				field.addValueChangeListener(event -> {
					for (String cascadePath : am.getCascadeAttributes()) {
						CascadeMode cm = am.getCascadeMode(cascadePath);
						if (CascadeMode.BOTH.equals(cm) || CascadeMode.EDIT.equals(cm)) {
							Field<?> cascadeField = groups.get(isViewMode()).getField(cascadePath);
							if (cascadeField instanceof Cascadable) {
								Cascadable ca = (Cascadable) cascadeField;
								if (event.getProperty().getValue() == null) {
									ca.clearAdditionalFilter();
								} else {
									ca.setAdditionalFilter(new Compare.Equal(am.getCascadeFilterPath(cascadePath),
											event.getProperty().getValue()));
								}
							} else {
								// field not found or does not support cascading
								throw new OCSRuntimeException(
										"Cannot setup cascading from " + am.getPath() + " to " + cascadePath);
							}
						}
					}
				});
			}
		}
	}

	/**
	 * Creates a custom field
	 * 
	 * @param entityModel
	 *            the entity model to base the field on
	 * @param attributeModel
	 *            the attribute model to base the field on
	 * @return
	 */
	protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode) {
		// by default, return null. override in subclasses in order to create
		// specific fields
		return null;
	}

	private Component constructImagePreview(AttributeModel attributeModel) {
		byte[] bytes = ClassUtils.getBytes(getEntity(), attributeModel.getName());
		Embedded image = new DefaultEmbedded(attributeModel.getDisplayName(), bytes);
		image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
		return image;
	}

	/**
	 * Constructs a field or label for a certain attribute
	 * 
	 * @param parent
	 *            the parent layout to which to add the field
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @param viewMode
	 *            whether the screen is in view mode
	 */
	@SuppressWarnings("unchecked")
	private void constructField(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, int tabIndex) {

		EntityModel<?> em = getFieldEntityModel(attributeModel);
		// allow the user to override the construction of a field
		Field<?> field = constructCustomField(entityModel, attributeModel, viewMode);
		if (field == null) {
			// if no custom field is defined, then use the default
			field = fieldFactory.constructField(attributeModel, getFieldFilters(), em);
		}

		if (field instanceof URLField) {
			((URLField) field)
					.setEditable(!isViewMode() && !EditableType.CREATE_ONLY.equals(attributeModel.getEditableType()));
		}

		// collection tables are normally rendered in both edit and view mode so
		// they must
		// be explicitly enabled/disabled
		if (field instanceof CollectionTable) {
			((CollectionTable<?>) field).setViewMode(isViewMode());
		}

		// set view mode if appropriate
		if (field instanceof QuickAddListSelect) {
			((QuickAddListSelect<?, ?>) field).setViewMode(isViewMode());
		}

		if (field != null) {
			groups.get(viewMode).bind(field, attributeModel.getName());
			field.setSizeFull();

			if (!attributeModel.getGroupTogetherWith().isEmpty()) {
				// multiple fields behind each other
				HorizontalLayout horizontal = constructRowLayout(attributeModel, attributeModel.isRequired(),
						!(field instanceof CheckBox));
				horizontal.setSizeFull();
				parent.addComponent(horizontal);

				// add the first field (without caption, unless it's a checkbox)
				if (!(field instanceof CheckBox)) {
					field.setCaption("");
				}

				// form layout for holding first field
				FormLayout fl = constructNestedFormLayout(true);
				fl.addComponent(field);
				horizontal.addComponent(fl);

				// form layout for any of the other fields
				for (String path : attributeModel.getGroupTogetherWith()) {
					AttributeModel am = getEntityModel().getAttributeModel(path);
					if (am != null) {
						FormLayout fl2 = constructNestedFormLayout(false);
						horizontal.addComponent(fl2);
						addField(fl2, entityModel, am, tabIndex);
					}
				}

			} else {
				parent.addComponent(field);
			}
		}

		// set the default value for new objects
		if (entity.getId() == null && attributeModel.getDefaultValue() != null) {
			field.getPropertyDataSource().setValue(attributeModel.getDefaultValue());
		}

		// store a reference to the first field so we can give it focus
		if (!isViewMode() && firstFields.get(tabIndex) == null && field.isEnabled() && !(field instanceof CheckBox)) {
			firstFields.put(tabIndex, field);
		}
	}

	/**
	 * Constructs a label
	 * 
	 * @param parent
	 *            the parent component to which the label must be added
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model for the attribute for which to create a label
	 * @param tabIndex
	 *            the number of components added so far
	 */
	private void constructLabel(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel,
			int tabIndex) {
		Component label = constructLabel(entity, attributeModel);
		labels.get(isViewMode()).put(attributeModel, label);

		if (!attributeModel.getGroupTogetherWith().isEmpty()) {
			// group multiple attributes on the same line
			HorizontalLayout horizontal = constructRowLayout(attributeModel, false, true);
			parent.addComponent(horizontal);

			label.setCaption("");

			FormLayout fl = constructNestedFormLayout(true);
			fl.addComponent(label);
			horizontal.addComponent(fl);

			for (String path : attributeModel.getGroupTogetherWith()) {
				AttributeModel am = entityModel.getAttributeModel(path);
				if (am != null) {
					FormLayout fl2 = constructNestedFormLayout(false);
					horizontal.addComponent(fl2);
					addField(fl2, getEntityModel(), am, tabIndex);
				}
			}
		} else {
			parent.addComponent(label);
		}
	}

	/**
	 * Constructs a form layout that is nested inside a horizontal layout when
	 * displaying multiple attributes in the same row
	 * 
	 * @param first
	 *            whether this is the layout for the first component
	 * @return
	 */
	private FormLayout constructNestedFormLayout(boolean first) {
		FormLayout fl = new FormLayout();
		if (first) {
			fl.setStyleName(DynamoConstants.CSS_FIRST, true);
		} else {
			fl.setStyleName(DynamoConstants.CSS_ADDITIONAL, true);
		}
		fl.setMargin(false);
		return fl;
	}

	/**
	 * Constructs a layout that serves as the basis for displaying multiple input
	 * components in a single row
	 * 
	 * @param attributeModel
	 *            the attribute model for the first attribute
	 * @param required
	 *            whether to mark the first field as required
	 * 
	 * @return
	 */
	private HorizontalLayout constructRowLayout(AttributeModel attributeModel, boolean required, boolean setCaption) {
		HorizontalLayout horizontal = new DefaultHorizontalLayout(false, true, true);
		if (setCaption) {
			horizontal.setCaption(attributeModel.getDisplayName());
		}
		horizontal.setStyleName(DynamoConstants.CSS_NESTED, true);
		if (required) {
			horizontal.setStyleName(DynamoConstants.CSS_REQUIRED, true);
		}
		return horizontal;
	}

	/**
	 * Constructs the save button
	 */
	private Button constructSaveButton() {
		Button saveButton = new Button(
				(entity != null && entity.getId() != null) ? message("ocs.save.existing") : message("ocs.save.new"));
		saveButton.addClickListener(event -> {
			try {
				boolean isNew = entity.getId() == null;
				entity = service.save(entity);
				setEntity(service.fetchById(entity.getId(), getDetailJoins()));
				showNotifification(message("ocs.changes.saved"), Notification.Type.TRAY_NOTIFICATION);

				// set to viewmode, load the view mode screen, and fill the
				// details
				if (getFormOptions().isOpenInViewMode()) {
					viewMode = true;
					build();
				}

				afterEditDone(false, isNew, getEntity());
			} catch (RuntimeException ex) {
				if (!handleCustomException(ex)) {
					handleSaveException(ex);
				}
			}
		});

		// enable/disable save button based on form validity
		saveButton.setData(SAVE_BUTTON_DATA);
		saveButton.setEnabled(groups.get(isViewMode()).isValid());
		for (Field<?> f : groups.get(isViewMode()).getFields()) {
			f.addValueChangeListener(event -> checkSaveButtonState());
		}
		return saveButton;
	}

	private Label constructTitleLabel() {
		Label label = null;

		// add title label
		String mainValue = EntityModelUtil.getMainAttributeValue(entity, getEntityModel());
		if (isViewMode()) {
			label = new Label(
					message("ocs.modelbasededitform.title.view", getEntityModel().getDisplayName(), mainValue),
					ContentMode.HTML);
		} else {
			if (entity.getId() == null) {
				// create a new entity
				label = new Label(message("ocs.modelbasededitform.title.create", getEntityModel().getDisplayName()),
						ContentMode.HTML);
			} else {
				// update an existing entity
				label = new Label(
						message("ocs.modelbasededitform.title.update", getEntityModel().getDisplayName(), mainValue),
						ContentMode.HTML);
			}
		}

		if (getFormTitleWidth() != null) {
			label.setWidth(getFormTitleWidth(), Unit.PIXELS);
		} else if (SystemPropertyUtils.getDefaultFormTitleWidth() > 0 && !getFormOptions().isPlaceButtonBarAtTop()) {
			label.setWidth(SystemPropertyUtils.getDefaultFormTitleWidth(), Unit.PIXELS);
		}

		return label;
	}

	/**
	 * Constructs an upload field
	 * 
	 * @param entityModel
	 * @param attributeModel
	 */
	private UploadComponent constructUploadField(AttributeModel attributeModel) {
		return new UploadComponent(attributeModel);
	}

	/**
	 * Disables any fields that are only editable when creating a new entity
	 */
	private void disableCreateOnlyFields() {
		if (!isViewMode()) {
			for (AttributeModel am : getEntityModel().getAttributeModels()) {
				Field<?> field = groups.get(isViewMode()).getField(am.getPath());
				if (field != null && EditableType.CREATE_ONLY.equals(am.getEditableType())) {
					field.setEnabled(entity.getId() == null);
				}
			}
		}
	}

	private List<Button> filterButtons(String data) {
		return Collections.unmodifiableList(
				buttons.get(isViewMode()).stream().filter(b -> data.equals(b.getData())).collect(Collectors.toList()));
	}

	public List<Button> getBackButtons() {
		return filterButtons(BACK_BUTTON_DATA);
	}

	public List<Button> getCancelButtons() {
		return filterButtons(CANCEL_BUTTON_DATA);
	}

	public FetchJoinInformation[] getDetailJoins() {
		return detailJoins;
	}

	public List<Button> getEditButtons() {
		return filterButtons(EDIT_BUTTON_DATA);
	}

	public T getEntity() {
		return entity;
	}

	public Field<?> getField(String propertyName) {
		return groups.get(isViewMode()).getField(propertyName);
	}

	public Integer getFormTitleWidth() {
		return formTitleWidth;
	}

	public Label getLabel(String propertyName) {
		AttributeModel am = getEntityModel().getAttributeModel(propertyName);
		if (am != null) {
			return (Label) labels.get(isViewMode()).get(am);
		}
		return null;
	}

	public List<Button> getNextButtons() {
		return filterButtons(NEXT_BUTTON_DATA);
	}

	/**
	 * Method that is called to select the next entity in a data set
	 * 
	 * @param current
	 *            the currently selected entity
	 * @return
	 */
	protected T getNextEntity(T current) {
		// overwrite in subclass
		return null;
	}

	/**
	 * Indicates which parent group a certain child group belongs to. The parent
	 * group must be mentioned in the result of the
	 * <code>getParentGroupHeaders</code> method. The childGroup must be the name of
	 * an attribute group from the entity model
	 * 
	 * @param childGroup
	 * @return
	 */
	protected String getParentGroup(String childGroup) {
		return null;
	}

	/**
	 * Returns the group headers of any additional parent groups that must be
	 * included in the form. These can be used to add an extra layer of nesting of
	 * the attribute groups
	 * 
	 * @return
	 */
	protected String[] getParentGroupHeaders() {
		return null;
	}

	/**
	 * Method that is called to select the previous entity in a data set
	 *
	 * @param current
	 *            the currently selected entity
	 * @return
	 */
	protected T getPrevEntity(T current) {
		// overwrite in subclass
		return null;
	}

	public List<Button> getPreviousButtons() {
		return filterButtons(PREV_BUTTON_DATA);
	}

	public List<Button> getSaveButtons() {
		return filterButtons(SAVE_BUTTON_DATA);
	}

	protected boolean hasNextEntity(T current) {
		return false;
	}

	protected boolean hasPrevEntity(T current) {
		return false;
	}

	protected boolean handleCustomException(RuntimeException ex) {
		return false;
	}

	/**
	 * Check whether a certain attribute group is visible
	 * 
	 * @param key
	 *            the message key by which the group is identifier
	 * @return
	 */
	public boolean isAttributeGroupVisible(String key) {
		Object c = attributeGroups.get(false).get(key);
		return c == null ? false : isGroupVisible(c);
	}

	/**
	 * Indicates whether it is allowed to edit this component
	 * 
	 * @return
	 */
	protected boolean isEditAllowed() {
		return true;
	}

	/**
	 * Check if a certain attribute group is visible
	 * 
	 * @param c
	 *            the component representing the attribute group
	 * @return
	 */
	private boolean isGroupVisible(Object c) {
		if (c != null) {
			if (c instanceof Component) {
				return ((Component) c).isVisible();
			} else if (c instanceof Tab) {
				return ((Tab) c).isVisible();
			}
		}
		return false;
	}

	public boolean isSupportsIteration() {
		return supportsIteration;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	/**
	 * Post-processes the button bar that is displayed above/below the edit form
	 * 
	 * @param buttonBar
	 * @param viewMode
	 */
	protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
		// overwrite in subclasses
	}

	/**
	 * Post-processes any edit fields- this method does nothing by default but must
	 * be used to call the postProcessEditFields callback method on an enclosing
	 * component
	 */
	protected void postProcessEditFields() {
		// overwrite in subclasses
	}

	/**
	 * Processes all fields that are part of a property group
	 * 
	 * @param parentGroupHeader
	 *            the group header
	 * @param innerForm
	 *            the form layout to which to add the fields
	 * @param innerTabs
	 *            whether we are displaying tabs
	 * @param innerTabSheet
	 *            the tab sheet to which to add the fields
	 * @param startCount
	 */
	private void processParentHeaderGroup(String parentGroupHeader, Layout innerForm, boolean innerTabs,
			TabSheet innerTabSheet, int tabIndex) {

		// display a group if it is not the default group
		for (String attributeGroup : getEntityModel().getAttributeGroups()) {
			if ((!EntityModel.DEFAULT_GROUP.equals(attributeGroup)
					|| getEntityModel().isAttributeGroupVisible(attributeGroup, viewMode))
					&& getParentGroup(attributeGroup).equals(parentGroupHeader)) {
				Layout innerLayout2 = constructAttributeGroupLayout(innerForm, innerTabs, innerTabSheet, attributeGroup,
						true);
				for (AttributeModel attributeModel : getEntityModel().getAttributeModelsForGroup(attributeGroup)) {
					addField(innerLayout2, getEntityModel(), attributeModel, tabIndex);
				}
			}
		}
	}

	/**
	 * Reconstructs all labels are a change of the view mode or the selected entity
	 */
	private void reconstructLabels() {
		// reconstruct all labels (since they cannot be bound automatically)
		if (labels.get(isViewMode()) != null) {
			for (Entry<AttributeModel, Component> e : labels.get(isViewMode()).entrySet()) {
				Component newLabel = constructLabel(entity, e.getKey());

				// label is displayed in view mode or when its an existing
				// entity
				newLabel.setVisible(entity.getId() != null || isViewMode());

				// copy custom style name (if any)
				if (e.getValue().getStyleName() != null) {
					newLabel.setStyleName(e.getValue().getStyleName());
				}

				// replace all existing labels with new labels
				HasComponents hc = e.getValue().getParent();
				if (hc instanceof Layout) {
					((Layout) hc).replaceComponent(e.getValue(), newLabel);
					labels.get(isViewMode()).put(e.getKey(), newLabel);
				}

				// hide caption for first label in a row
				if (newLabel.getParent() instanceof FormLayout) {
					FormLayout fl = (FormLayout) newLabel.getParent();
					if (fl.getStyleName().contains("first")) {
						newLabel.setCaption("");
					}
				}
			}
		}

		// also replace the title label
		Label titleLabel = titleLabels.get(isViewMode());
		if (titleLabel != null) {
			Label newLabel = constructTitleLabel();
			titleLabels.put(isViewMode(), newLabel);
			titleBars.get(isViewMode()).replaceComponent(titleLabel, newLabel);
		}
	}

	/**
	 * Replaces a label (in response to a change)
	 * 
	 * @param propertyName
	 */
	public void refreshLabel(String propertyName) {
		AttributeModel am = getEntityModel().getAttributeModel(propertyName);
		if (am != null) {
			Component replacement = constructLabel(getEntity(), am);
			Component oldLabel = labels.get(isViewMode()).get(am);

			// label is displayed in view mode or when its an existing entity
			replacement.setVisible(true);

			// replace all existing labels with new labels
			HasComponents hc = labels.get(isViewMode()).get(am).getParent();
			if (hc instanceof Layout) {
				((Layout) hc).replaceComponent(oldLabel, replacement);
				labels.get(isViewMode()).put(am, replacement);
			}
		}
	}

	/**
	 * Replaces an existing label by a label with the provided value
	 * 
	 * @param propertyName
	 *            the name of the property for which to replace the label
	 * @param value
	 *            the name value
	 */
	public void refreshLabel(String propertyName, String value) {
		AttributeModel am = getEntityModel().getAttributeModel(propertyName);
		if (am != null) {
			Component replacement = new Label(value);
			replacement.setCaption(am.getDisplayName());
			Component oldLabel = labels.get(isViewMode()).get(am);

			// label is displayed in view mode or when its an existing entity
			replacement.setVisible(true);

			// replace all existing labels with new labels
			HasComponents hc = labels.get(isViewMode()).get(am).getParent();
			if (hc instanceof Layout) {
				((Layout) hc).replaceComponent(oldLabel, replacement);
				labels.get(isViewMode()).put(am, replacement);
			}
		}
	}

	/**
	 * Resets the selected tab index
	 */
	public void resetTab() {
		if (tabSheets.get(isViewMode()) != null && !getFormOptions().isPreserveSelectedTab()) {
			tabSheets.get(isViewMode()).setSelectedTab(0);
		}
	}

	/**
	 * Selects the tab specified by the provided index
	 * 
	 * @param index
	 */
	public void selectTab(int index) {
		if (tabSheets.get(isViewMode()) != null) {
			tabSheets.get(isViewMode()).setSelectedTab(index);
		}
	}

	/**
	 * Shows/hides an attribute group
	 * 
	 * @param caption
	 *            the message key by which the group is identified
	 * @param visible
	 *            whether to show/hide the group
	 */
	public void setAttributeGroupVisible(String key, boolean visible) {
		Object c = attributeGroups.get(false).get(key);
		setGroupVisible(c, visible);
		c = attributeGroups.get(true).get(key);
		setGroupVisible(c, visible);
	}

	/**
	 * Shows or hides the component for a certain property - this will work
	 * regardless of the view
	 * 
	 * @param propertyName
	 *            the name of the property for which to show/hide the property
	 * @param visible
	 */
	public void setComponentVisible(String propertyName, boolean visible) {
		setLabelVisible(propertyName, visible);
		Field<?> field = getField(propertyName);
		if (field != null) {
			field.setVisible(visible);
		}
	}

	public void setDetailJoins(FetchJoinInformation[] detailJoins) {
		this.detailJoins = detailJoins;
	}

	public void setEntity(T entity) {
		setEntity(entity, entity.getId() != null);
	}

	private void setEntity(T entity, boolean checkIterationButtons) {
		this.entity = entity;
		afterEntitySet(this.entity);

		setViewMode(getFormOptions().isOpenInViewMode() && entity.getId() != null, checkIterationButtons);

		// recreate the group
		BeanItem<T> beanItem = new BeanItem<>(entity);
		groups.get(isViewMode()).setItemDataSource(beanItem);

		// "rebuild" so that the correct layout is displayed
		build();
		reconstructLabels();

		// refresh the upload components
		for (Entry<AttributeModel, Component> e : uploads.get(isViewMode()).entrySet()) {
			HasComponents hc = e.getValue().getParent();
			if (hc instanceof Layout) {
				Component uc = constructUploadField(e.getKey());
				((Layout) hc).replaceComponent(e.getValue(), uc);
				uploads.get(isViewMode()).put(e.getKey(), uc);
			}
		}

		// refresh preview components
		for (Entry<AttributeModel, Component> e : previews.get(isViewMode()).entrySet()) {
			HasComponents hc = e.getValue().getParent();
			if (hc instanceof Layout) {
				Component pv = constructImagePreview(e.getKey());
				((Layout) hc).replaceComponent(e.getValue(), pv);
				previews.get(isViewMode()).put(e.getKey(), pv);
			}
		}

		// refresh any fields that need it
		for (Field<?> f : groups.get(isViewMode()).getFields()) {
			if (f instanceof Refreshable) {
				((Refreshable) f).refresh();
			}
		}

		// enable/disable fields for create only mode
		disableCreateOnlyFields();

		// update the title label
		Label newTitleLabel = constructTitleLabel();
		titleBars.get(isViewMode()).replaceComponent(titleLabels.get(isViewMode()), newTitleLabel);
		titleLabels.put(isViewMode(), newTitleLabel);

		// change caption depending on entity state
		updateSaveButtonCaptions();

		for (Button b : getCancelButtons()) {
			b.setVisible((!isViewMode() && !getFormOptions().isHideCancelButton())
					|| (getFormOptions().isFormNested() && entity.getId() == null));
		}

	}

	public void setFormTitleWidth(Integer formTitleWidth) {
		this.formTitleWidth = formTitleWidth;
	}

	/**
	 * Hides/shows a group of components
	 * 
	 * @param c
	 *            the parent component of the group
	 * @param visible
	 *            whether to set the component to visible
	 */
	private void setGroupVisible(Object c, boolean visible) {
		if (c != null) {
			if (c instanceof Component) {
				((Component) c).setVisible(visible);
			} else if (c instanceof Tab) {
				((Tab) c).setVisible(visible);
			}
		}
	}

	/**
	 * Shows or hides a label
	 * 
	 * @param propertyName
	 *            the name of the property for which to show/hide the label
	 * @param visible
	 *            whether to show the label
	 */
	public void setLabelVisible(String propertyName, boolean visible) {
		AttributeModel am = getEntityModel().getAttributeModel(propertyName);
		if (am != null) {
			Component label = labels.get(isViewMode()).get(am);
			if (label != null) {
				if (ClassUtils.getFieldValue(entity, propertyName) != null) {
					label.setVisible(visible);
					label.setCaption(am.getDisplayName());
				} else {
					label.setVisible(false);
					label.setCaption(null);
				}
			}

		}
	}

	public void setSupportsIteration(boolean supportsIteration) {
		this.supportsIteration = supportsIteration;
	}

	public void setViewMode(boolean viewMode) {
		setViewMode(viewMode, true);
	}

	/**
	 * Switches the form from or to view mode
	 * 
	 * @param viewMode
	 *            the new view mode
	 */
	private void setViewMode(boolean viewMode, boolean checkIterationButtons) {
		boolean oldMode = this.viewMode;

		// check what the new view mode must become and adapt the screen
		this.viewMode = !isEditAllowed() || viewMode;

		BeanItem<T> beanItem = new BeanItem<>(entity);
		groups.get(isViewMode()).setItemDataSource(beanItem);

		constructTitleLabel();
		reconstructLabels();
		build();

		checkIterationButtonState(checkIterationButtons);

		// if this is the first time in edit mode, post process the editable
		// fields
		if (!isViewMode() && !fieldsProcessed) {
			postProcessEditFields();
			fieldsProcessed = true;
		}

		// update button captions
		updateSaveButtonCaptions();
		disableCreateOnlyFields();

		// preserve tab index when switching
		if (tabSheets.get(oldMode) != null) {
			Component c = tabSheets.get(oldMode).getSelectedTab();
			int index = VaadinUtils.getTabIndex(tabSheets.get(oldMode), tabSheets.get(oldMode).getTab(c).getCaption());
			tabSheets.get(this.viewMode).setSelectedTab(index);

			// focus first field
			if (!isViewMode() && firstFields.get(index) != null) {
				firstFields.get(index).focus();
			}
		} else if (firstFields.get(0) != null) {
			firstFields.get(0).focus();
		}

		if (oldMode != this.viewMode) {
			afterModeChanged(isViewMode());
		}
	}

	/**
	 * Method used the notify this form that a detail component is valid or not
	 * 
	 * @param component
	 *            the component
	 * @param valid
	 *            whether the component is valid
	 */
	public void signalDetailsComponentValid(SignalsParent component, boolean valid) {
		detailTablesValid.put(component, valid);
		checkSaveButtonState();
	}

	/**
	 * Apply styling to a label
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @param className
	 *            the name of the CSS class to add
	 */
	public void styleLabel(String propertyName, String className) {
		AttributeModel am = getEntityModel().getAttributeModel(propertyName);
		if (am != null) {
			Component editLabel = labels.get(false) == null ? null : labels.get(false).get(am);
			Component viewLabel = labels.get(true) == null ? null : labels.get(true).get(am);

			if (editLabel != null) {
				editLabel.addStyleName(className);
			}

			if (viewLabel != null) {
				viewLabel.addStyleName(className);
			}
		}
	}

	/**
	 * Sets the caption of the save button depending on whether we are creating or
	 * updating an entity
	 */
	private void updateSaveButtonCaptions() {
		for (Button b : getSaveButtons()) {
			if (entity.getId() != null) {
				b.setCaption(message("ocs.save.existing"));
			} else {
				b.setCaption(message("ocs.save.new"));
			}
		}
	}

	public boolean isNestedMode() {
		return nestedMode;
	}

	public void setNestedMode(boolean nestedMode) {
		this.nestedMode = nestedMode;
	}

	public Collection<Field<?>> getFields(boolean viewMode) {
		return groups.get(viewMode).getFields();
	}

}
