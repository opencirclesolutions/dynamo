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
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.vaadin.teemu.switchui.Switch;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.EqualsPredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.CanAssignEntity;
import com.ocs.dynamo.ui.Refreshable;
import com.ocs.dynamo.ui.component.Cascadable;
import com.ocs.dynamo.ui.component.CollapsiblePanel;
import com.ocs.dynamo.ui.component.CustomEntityField;
import com.ocs.dynamo.ui.component.DefaultEmbedded;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.type.AttributeGroupMode;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Embedded;
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
 * An edit form that is constructed based on an entity model s
 * 
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 * @author bas.rutten
 */
@SuppressWarnings("serial")
public class ModelBasedEditForm<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractModelBasedForm<ID, T> implements NestedComponent {

	/**
	 * A custom field that can be used to upload a file
	 *
	 * @author bas.ruttent
	 */
	private final class UploadComponent extends CustomField<byte[]> {

		private AttributeModel attributeModel;

		private UploadComponent(AttributeModel attributeModel) {
			this.attributeModel = attributeModel;
		}

		@Override
		protected void doSetValue(byte[] value) {
			// not needed
		}

		@Override
		public byte[] getValue() {
			// not needed
			return null;
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
		 * @param target              the target component that must be updated after an
		 *                            upload
		 * @param fieldName           the name of the field
		 * @param supportedExtensions the supported file extensions
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

				if (supportedExtensions == null || supportedExtensions.length == 0 || (extension != null
						&& Arrays.asList(supportedExtensions).contains(extension.toLowerCase()))) {

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

					if (afterUploadConsumer != null) {
						afterUploadConsumer.accept(event.getFilename(), stream.toByteArray());
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
	private Map<NestedComponent, Boolean> detailComponentsValid = new HashMap<>();

	/**
	 * The selected entity
	 */
	private T entity;

	/**
	 * The field factory
	 */
	private FieldFactory fieldFactory = FieldFactory.getInstance();

	/**
	 * Indicates whether the fields have been post processed
	 */
	private boolean fieldsProcessed;

	/**
	 * Groups for data binding (one for each view mode)
	 */
	private Map<Boolean, Binder<T>> groups = new HashMap<>();

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

	/**
	 * Map from tab index to the first field on each tab
	 */
	private Map<Integer, Focusable> firstFields = new HashMap<>();

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
	 * The fields to which to assign the currently selected entity after the
	 * selected entity changes
	 */
	private List<CanAssignEntity<ID, T>> assignEntityToFields = new ArrayList<>();

	/**
	 * Whether the form is in nested mode
	 */
	private boolean nestedMode;

	/**
	 * Custom consumer that is to be called instead of the regular save behaviour
	 */
	private Consumer<T> customSaveConsumer;

	private BiConsumer<String, byte[]> afterUploadConsumer;

	/**
	 * Constructor
	 *
	 * @param entity       the entity
	 * @param service      the service
	 * @param entityModel  the entity model
	 * @param formOptions  the form options
	 * @param fieldFilters the field filters
	 */
	public ModelBasedEditForm(T entity, BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			Map<String, SerializablePredicate<?>> fieldFilters) {
		super(formOptions, fieldFilters, entityModel);
		this.service = service;
		this.entity = entity;
		afterEntitySet(entity);
		Class<T> clazz = service.getEntityClass();

		// open in view mode when this is requested, and it is not a new object
		this.viewMode = !isEditAllowed() || (formOptions.isOpenInViewMode() && entity.getId() != null);

		// set up a bean field group for automatic binding and validation
		Binder<T> binder = new BeanValidationBinder<>(clazz);
		binder.setBean(entity);
		groups.put(Boolean.FALSE, binder);

		binder = new BeanValidationBinder<>(clazz);
		binder.setBean(entity);
		groups.put(Boolean.TRUE, binder);

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
	 * @param parent         the layout to which to add the field
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 */
	private void addField(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel, int tabIndex,
			boolean sameRow) {
		AttributeType type = attributeModel.getAttributeType();
		if (!alreadyBound.get(isViewMode()).contains(attributeModel.getPath()) && attributeModel.isVisible()
				&& (AttributeType.BASIC.equals(type) || AttributeType.LOB.equals(type)
						|| attributeModel.isComplexEditable())) {
			if (EditableType.READ_ONLY.equals(attributeModel.getEditableType()) || isViewMode()) {
				if (attributeModel.isUrl() || attributeModel.isNavigable()) {
					// display a complex component even in read-only mode
					constructField(parent, entityModel, attributeModel, true, tabIndex, sameRow);
				} else if (AttributeType.LOB.equals(type) && attributeModel.isImage()) {
					// image preview
					Component c = constructImagePreview(attributeModel);
					parent.addComponent(c);
					previews.get(isViewMode()).put(attributeModel, c);
				} else if (AttributeType.LOB.equals(type) && attributeModel.isImage()) {
					// image preview
					Component c = constructImagePreview(attributeModel);
					parent.addComponent(c);
					previews.get(isViewMode()).put(attributeModel, c);
				} else if (AttributeType.DETAIL.equals(type) && attributeModel.isComplexEditable()) {
					AbstractComponent f = constructCustomField(entityModel, attributeModel, viewMode);
					if (f instanceof UseInViewMode) {
						// a details edit table or details edit layout must always be displayed
						constructField(parent, entityModel, attributeModel, true, tabIndex, sameRow);
					} else {
						constructLabel(parent, entityModel, attributeModel, tabIndex, sameRow);
					}
				} else {
					AbstractComponent f = constructCustomField(entityModel, attributeModel, viewMode);
					if (f instanceof UseInViewMode) {
						constructField(parent, entityModel, attributeModel, true, tabIndex, sameRow);
					} else {// otherwise display a label
						constructLabel(parent, entityModel, attributeModel, tabIndex, sameRow);
					}
				}
			} else {
				// display an editable field
				if (AttributeType.BASIC.equals(type) || AttributeType.MASTER.equals(type)
						|| AttributeType.DETAIL.equals(type) || AttributeType.ELEMENT_COLLECTION.equals(type)) {
					constructField(parent, entityModel, attributeModel, false, tabIndex, sameRow);
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
				afterTabSelected(index);
				if (firstFields.get(index) != null) {
					firstFields.get(index).focus();
				}
			}
		});
	}

	/**
	 * Method that is called after the user is done editing an entity
	 *
	 * @param cancel    whether the user cancelled the editing
	 * @param newObject whether the object is a new object
	 * @param entity    the entity
	 */
	protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
		// override in subclass
	}

	/**
	 * Respond to the setting of a new entity as the selected entity. This can be
	 * used to fetch any additionally required data
	 *
	 * @param entity the entity
	 */
	protected void afterEntitySet(T entity) {
		// override in subclass
	}

	/**
	 * Method that is called after a layout is built for the first time
	 * 
	 * @param layout   the layout that has just been built
	 * @param viewMode whether the form is currently in view mode
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
	 * Callback method that is called after a tab has been selected
	 *
	 * @param tabIndex the zero-based index of the selected tab
	 */
	protected void afterTabSelected(int tabIndex) {
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
				for (CanAssignEntity<ID, T> field : assignEntityToFields) {
					field.assignEntity(entity);
				}

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
			buttonBar = constructButtonBar(false);
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
							addField(innerForm, entityModel, attributeModel, tabIndex, false);
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
				addField(form, entityModel, attributeModel, 0, false);
			}
		}

		constructCascadeListeners();
		layout.addComponent(form);

		if (firstFields.get(0) != null) {
			firstFields.get(0).focus();
		}

		if (!nestedMode) {
			buttonBar = constructButtonBar(true);
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
	 * @param checkEnabled whether to check if the buttons should be enabled
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
	 * Sets the state (enabled/disabled) of the save button. The button is* enabled*
	 * if the from is valid and all of its detail tables are as well
	 */
	private void checkSaveButtonState() {
		for (Button saveButton : buttons.get(isViewMode())) {
			if (SAVE_BUTTON_DATA.equals(saveButton.getData())) {
				saveButton.setEnabled(true);
			}
		}
	}

	/**
	 * Construct the layout (form and panel) for an attribute group
	 *
	 * @param parent     the parent component
	 * @param tabs       whether to include the component in a tab sheet
	 * @param tabSheet   the parent tab sheet (only used if the "tabs" parameter is
	 *                   true)
	 * @param messageKey caption of the panel or tab sheet
	 * @param lowest     indicates whether this is the lowest level
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
			innerLayout = new DefaultVerticalLayout(false, true);
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

	private HorizontalLayout constructButtonBar(boolean bottom) {
		HorizontalLayout buttonBar = new DefaultHorizontalLayout();

		// button to go back to the main screen when in view mode
		Button backButton = new Button(message("ocs.back"));
		backButton.setIcon(VaadinIcons.BACKWARDS);
		backButton.addClickListener(event -> back());
		backButton.setVisible(isViewMode() && getFormOptions().isShowBackButton());
		backButton.setData(BACK_BUTTON_DATA);
		buttonBar.addComponent(backButton);
		buttons.get(isViewMode()).add(backButton);

		// in edit mode, display a cancel button
		Button cancelButton = new Button(message("ocs.cancel"));
		cancelButton.setData(CANCEL_BUTTON_DATA);
		cancelButton.addClickListener(event -> {
			if (entity.getId() != null) {
				entity = service.fetchById(entity.getId(), getDetailJoins());
			}
			afterEditDone(true, entity.getId() == null, entity);
		});
		cancelButton.setVisible((!isViewMode() && !getFormOptions().isHideCancelButton())
				|| (getFormOptions().isFormNested() && entity.getId() == null));
		cancelButton.setIcon(VaadinIcons.BAN);
		buttonBar.addComponent(cancelButton);
		buttons.get(isViewMode()).add(cancelButton);

		// create the save button
		if (!isViewMode()) {
			Button saveButton = constructSaveButton(bottom);
			buttonBar.addComponent(saveButton);
			buttons.get(isViewMode()).add(saveButton);

		}

		// create the edit button
		Button editButton = new Button(message("ocs.edit"));
		editButton.setIcon(VaadinIcons.PENCIL);
		editButton.addClickListener(event -> setViewMode(false));
		buttonBar.addComponent(editButton);
		buttons.get(isViewMode()).add(editButton);
		editButton.setData(EDIT_BUTTON_DATA);
		editButton.setVisible(isViewMode() && getFormOptions().isEditAllowed() && isEditAllowed());

		// button for moving to the previous record
		Button prevButton = new Button(message("ocs.previous"));
		prevButton.setIcon(VaadinIcons.ARROW_LEFT);
		prevButton.addClickListener(e -> {
			T prev = getPreviousEntity();
			if (prev != null) {
				setEntity(prev, true);
			}
			getPreviousButtons().stream().forEach(b -> b.setEnabled(hasPrevEntity()));
		});
		prevButton.setData(PREV_BUTTON_DATA);
		buttons.get(isViewMode()).add(prevButton);
		buttonBar.addComponent(prevButton);
		prevButton.setEnabled(hasPrevEntity());

		// button for moving to the next record
		Button nextButton = new Button(message("ocs.next"));
		nextButton.setIcon(VaadinIcons.ARROW_RIGHT);
		nextButton.addClickListener(e -> {
			T next = getNextEntity();
			if (next != null) {
				setEntity(next, true);
			}
			getNextButtons().stream().forEach(b -> b.setEnabled(hasNextEntity()));

		});
		nextButton.setEnabled(hasNextEntity());
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
	@SuppressWarnings("unchecked")
	private <S> void constructCascadeListeners() {
		for (final AttributeModel am : getEntityModel().getCascadeAttributeModels()) {
			HasValue<S> field = (HasValue<S>) getField(isViewMode(), am.getPath());
			if (field != null) {
				field.addValueChangeListener(event -> {
					for (String cascadePath : am.getCascadeAttributes()) {
						CascadeMode cm = am.getCascadeMode(cascadePath);
						if (CascadeMode.BOTH.equals(cm) || CascadeMode.EDIT.equals(cm)) {
							AbstractComponent cascadeField = getField(isViewMode(), cascadePath);
							if (cascadeField instanceof Cascadable) {
								Cascadable<S> ca = (Cascadable<S>) cascadeField;
								if (event.getValue() == null) {
									ca.clearAdditionalFilter();
								} else {
									ca.setAdditionalFilter(new EqualsPredicate<S>(am.getCascadeFilterPath(cascadePath),
											event.getValue()));
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
	 * @param entityModel    the entity model to base the field on
	 * @param attributeModel the attribute model to base the field on
	 * @return
	 */
	protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode) {
		// by default, return null. override in subclasses in order to create
		// specific fields
		return null;
	}

	/**
	 * Constructs a field or label for a certain attribute
	 *
	 * @param parent         the parent layout to which to add the field
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param viewMode       whether the screen is in view mode
	 */
	@SuppressWarnings({ "unchecked" })
	private void constructField(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, int tabIndex, boolean sameRow) {

		EntityModel<?> fieldEntityModel = getFieldEntityModel(attributeModel);
		// allow the user to override the construction of a field
		AbstractComponent field = constructCustomField(entityModel, attributeModel, viewMode);
		if (field == null) {
			FieldFactoryContext ctx = FieldFactoryContext.create().setAttributeModel(attributeModel)
					.setFieldEntityModel(fieldEntityModel).setFieldFilters(getFieldFilters()).setViewMode(viewMode);
			field = fieldFactory.constructField(ctx);
		}

		if (field instanceof URLField) {
			((URLField) field)
					.setEditable(!isViewMode() && !EditableType.CREATE_ONLY.equals(attributeModel.getEditableType()));
		}

		if (field != null) {
			// set explicit field width
			if (!(field instanceof Switch)) {
				Integer fieldWidth = SystemPropertyUtils.getDefaultFieldWidth();
				if (fieldWidth == null || sameRow || field instanceof FullWidthField
						|| !attributeModel.getGroupTogetherWith().isEmpty()) {
					field.setSizeFull();
				} else {
					field.setWidth(fieldWidth + "px");
				}
			}

			// add converters and validators
			BindingBuilder<T, ?> builder = groups.get(viewMode).forField((HasValue<?>) field);
			FieldFactoryImpl.addConvertersAndValidators(builder, attributeModel,
					constructCustomConverter(attributeModel));

			builder.bind(attributeModel.getPath());

			if (!attributeModel.getGroupTogetherWith().isEmpty()) {
				// multiple fields behind each other
				HorizontalLayout horizontal = constructRowLayout(attributeModel, attributeModel.isRequired(),
						!(field instanceof CheckBox));
				Integer fieldWidth = SystemPropertyUtils.getDefaultFieldWidth();
				if (fieldWidth == null || nestedMode) {
					horizontal.setSizeFull();
				} else {
					horizontal.setWidth(fieldWidth + "px");
				}
				parent.addComponent(horizontal);

				// add the first field (without caption, unless it's a check box)
				if (!(field instanceof CheckBox)) {
					field.setCaption("");
				}

				// calculate expansion factors
				float sums = attributeModel.getExpansionFactor();
				for (String path : attributeModel.getGroupTogetherWith()) {
					AttributeModel am = getEntityModel().getAttributeModel(path);
					if (am != null) {
						sums += am.getExpansionFactor();
					}
				}

				// form layout for holding first field
				FormLayout fl = constructNestedFormLayout(true);
				fl.addComponent((AbstractComponent) field);
				horizontal.addComponent(fl);

				float ep = attributeModel.getExpansionFactor() / sums;
				horizontal.setExpandRatio(fl, ep);// form layout for any of the other fields
				for (String path : attributeModel.getGroupTogetherWith()) {
					AttributeModel nestedAm = getEntityModel().getAttributeModel(path);
					if (nestedAm != null) {
						FormLayout fl2 = constructNestedFormLayout(false);
						horizontal.addComponent(fl2);
						ep = nestedAm.getExpansionFactor() / sums;
						addField(fl2, entityModel, nestedAm, tabIndex, true);
						horizontal.setExpandRatio(fl2, ep);
					}
				}
			} else {
				parent.addComponent((AbstractComponent) field);
			}
		}

		// set the default value for new objects
		if (entity.getId() == null && attributeModel.getDefaultValue() != null) {
			setDefaultValue((HasValue<Object>) field, attributeModel.getDefaultValue());
		}

		// store a reference to the first field so we can give it focus
		if (!isViewMode() && firstFields.get(tabIndex) == null && field.isEnabled() && !(field instanceof CheckBox)
				&& (field instanceof Focusable)) {
			firstFields.put(tabIndex, (Focusable) field);
		}

		if (field instanceof CanAssignEntity) {
			assignEntityToFields.add((CanAssignEntity<ID, T>) field);
		}
	}

	/**
	 * Callback method for inserting custom converter
	 * 
	 * @param am
	 * @return
	 */
	protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
		return null;
	}

	/**
	 * Constructs a preview component for displaying an image
	 * 
	 * @param attributeModel the attribute model for the image property
	 * @return
	 */
	private Component constructImagePreview(AttributeModel attributeModel) {
		byte[] bytes = ClassUtils.getBytes(getEntity(), attributeModel.getName());
		Embedded image = new DefaultEmbedded(attributeModel.getDisplayName(), bytes);
		image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
		return image;
	}

	/**
	 * Constructs a label
	 *
	 * @param parent         the parent component to which the label must be added
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model for the attribute for which to
	 *                       create a* label
	 * @param tabIndex       the number of components added so far
	 */
	private void constructLabel(Layout parent, EntityModel<T> entityModel, AttributeModel attributeModel, int tabIndex,
			boolean sameRow) {
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
					addField(fl2, getEntityModel(), am, tabIndex, sameRow);
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
	 * @param first whether this is the layout for the first component
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
	 * Constructs a layout that serves as the basis for displaying multiple* input*
	 * components in a single row
	 *
	 * @param attributeModel the attribute model for the first attribute
	 * @param required       whether to mark the first field as required
	 * @return
	 */
	private HorizontalLayout constructRowLayout(AttributeModel attributeModel, boolean required, boolean setCaption) {
		HorizontalLayout horizontal = new DefaultHorizontalLayout(false, true, true);
		if (setCaption) {
			horizontal.setCaption(attributeModel.getDisplayName());
		}
		horizontal.setStyleName(DynamoConstants.CSS_NESTED, true);
		if (required) {
			horizontal.setStyleName(DynamoConstants.CSS_NESTED + " " + DynamoConstants.CSS_REQUIRED, true);

		}
		return horizontal;
	}

	/**
	 * Constructs the save button
	 *
	 * @param bottom indicates whether this is the button at the bottom of the
	 *               screen
	 */
	private Button constructSaveButton(boolean bottom) {
		Button saveButton = new Button(
				(entity != null && entity.getId() != null) ? message("ocs.save.existing") : message("ocs.save.new"));
		saveButton.setIcon(VaadinIcons.SAFE);
		saveButton.addClickListener(event -> {
			try {

				// validate all fields
				boolean error = validateAllFields();
				if (!error) {
					if (getFormOptions().isConfirmSave()) {
						// ask for confirmation before saving

						service.validate(entity);
						VaadinUtils.showConfirmDialog(getMessageService(), getMessageService().getMessage(
								"ocs.confirm.save", VaadinUtils.getLocale(), getEntityModel().getDisplayName()), () -> {
									try {
										if (customSaveConsumer != null) {
											customSaveConsumer.accept(entity);
										} else {
											doSave();
										}
									} catch (RuntimeException ex) {
										if (!handleCustomException(ex)) {
											handleSaveException(ex);
										}
									}
								});
					} else {
						if (customSaveConsumer != null) {
							customSaveConsumer.accept(entity);
						} else {
							doSave();
						}
					}
				}
			} catch (RuntimeException ex) {
				if (!handleCustomException(ex)) {
					handleSaveException(ex);
				}
			}
		});

		// enable/disable save button based on form validity
		saveButton.setData(SAVE_BUTTON_DATA);
		// saveButton.setEnabled(groups.get(isViewMode()).isValid());
		if (bottom) {
			groups.get(isViewMode()).getFields().forEach(f -> f.addValueChangeListener(event -> {
				checkSaveButtonState();
				((AbstractComponent) f).setComponentError(null);
			}));
		}
		return saveButton;
	}

	private Label constructTitleLabel() {
		Label label = null;

		// add title label
		String mainValue = EntityModelUtils.getMainAttributeValue(entity, getEntityModel());
		if (isViewMode()) {
			label = new Label(
					message("ocs.modelbasededitform.title.view", getEntityModel().getDisplayName(), mainValue));
		} else {
			if (entity.getId() == null) {
				// create a new entity
				label = new Label(message("ocs.modelbasededitform.title.create", getEntityModel().getDisplayName()));
			} else {
				// update an existing entity
				label = new Label(
						message("ocs.modelbasededitform.title.update", getEntityModel().getDisplayName(), mainValue));
			}
		}
		label.setContentMode(ContentMode.HTML);

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
				AbstractComponent field = getField(isViewMode(), am.getPath());
				if (field != null && EditableType.CREATE_ONLY.equals(am.getEditableType())) {
					field.setEnabled(entity.getId() == null);
				}
			}
		}
	}

	/**
	 * Perform the actual save action
	 */
	public void doSave() {
		boolean isNew = entity.getId() == null;
		entity = service.save(entity);
		setEntity(service.fetchById(entity.getId(), getDetailJoins()));
		showNotifification(message("ocs.changes.saved"), Notification.Type.TRAY_NOTIFICATION);

		// set to view mode, load the view mode screen, and fill the
		// details
		if (getFormOptions().isOpenInViewMode()) {
			viewMode = true;
			build();
		}
		afterEditDone(false, isNew, getEntity());
	}

	private List<Button> filterButtons(String data) {
		return Collections.unmodifiableList(
				buttons.get(isViewMode()).stream().filter(b -> data.equals(b.getData())).collect(Collectors.toList()));
	}

	public BiConsumer<String, byte[]> getAfterUploadConsumer() {
		return afterUploadConsumer;
	}

	public CollapsiblePanel getAttributeGroupPanel(String key) {
		Object c = attributeGroups.get(isViewMode()).get(key);
		if (c instanceof CollapsiblePanel) {
			return (CollapsiblePanel) c;
		}
		return null;
	}

	public List<Button> getBackButtons() {
		return filterButtons(BACK_BUTTON_DATA);
	}

	public List<Button> getCancelButtons() {
		return filterButtons(CANCEL_BUTTON_DATA);
	}

	public Consumer<T> getCustomSaveConsumer() {
		return customSaveConsumer;
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

	/**
	 * Retrieves a field for a certain property
	 * 
	 * @param viewMode  whether the screen is in view mode
	 * @param fieldName the name of the field/property
	 * @return
	 */
	private AbstractComponent getField(boolean viewMode, String fieldName) {
		Optional<Binding<T, ?>> binding = groups.get(viewMode).getBinding(fieldName);
		if (binding.isPresent()) {
			return (AbstractComponent) binding.get().getField();
		}
		return null;
	}

	/**
	 * Returns the binding for a field
	 * 
	 * @param fieldName
	 * @return
	 */
	public Binding<T, ?> getBinding(String fieldName) {
		Optional<Binding<T, ?>> binding = groups.get(viewMode).getBinding(fieldName);
		if (binding.isPresent()) {
			return binding.get();
		}
		return null;
	}

	public AbstractComponent getField(String fieldName) {
		return getField(isViewMode(), fieldName);
	}

	public Optional<AbstractComponent> getFieldOptional(String fieldName) {
		return Optional.ofNullable(getField(isViewMode(), fieldName));
	}

	public Collection<HasValue<?>> getFields(boolean viewMode) {
		return groups.get(viewMode).getFields().collect(Collectors.toList());
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
	 * Returns the next entity from the encapsulating layout
	 * 
	 * @return
	 */
	protected T getNextEntity() {
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

	public List<Button> getPreviousButtons() {
		return filterButtons(PREV_BUTTON_DATA);
	}

	/**
	 * Returns the previous entity from the encapsulating layout
	 * 
	 * @return
	 */
	protected T getPreviousEntity() {
		// overwrite in subclass
		return null;
	}

	public List<Button> getSaveButtons() {
		return filterButtons(SAVE_BUTTON_DATA);
	}

	public int getSelectedTabIndex() {
		if (tabSheets.get(isViewMode()) != null) {
			Component c = tabSheets.get(isViewMode()).getSelectedTab();
			return VaadinUtils.getTabIndex(tabSheets.get(isViewMode()),
					tabSheets.get(isViewMode()).getTab(c).getCaption());
		}
		return 0;
	}

	protected boolean handleCustomException(RuntimeException ex) {
		return false;
	}

	protected boolean hasNextEntity() {
		return false;
	}

	protected boolean hasPrevEntity() {
		return false;
	}

	/**
	 * Check whether a certain attribute group is visible
	 *
	 * @param key the message key by which the group is identifier
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
	 * @param c the component representing the attribute group
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

	public boolean isNestedMode() {
		return nestedMode;
	}

	public boolean isSupportsIteration() {
		return supportsIteration;
	}

	/**
	 * Check if the form is valid
	 *
	 * @return
	 */
	public boolean isValid() {
		boolean valid = groups.get(isViewMode()).isValid();
		valid &= detailComponentsValid.values().stream().allMatch(x -> x);
		return valid;
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
	 * @param parentGroupHeader the group header
	 * @param innerForm         the form layout to which to add the fields
	 * @param innerTabs         whether we are displaying tabs
	 * @param innerTabSheet     the tab sheet to which to add the fields
	 * @param tabIndex
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
					addField(innerLayout2, getEntityModel(), attributeModel, tabIndex, false);
				}
			}
		}
	}

	public void putAttributeGroupPanel(String key, Component c) {
		attributeGroups.get(isViewMode()).put(key, c);
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
	 * TODO: does not seem to work properly
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
	 * @param propertyName the name of the property for which to replace the label
	 * @param value        the name value
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
	 * Removes any error messages from the individual form components
	 */
	private void resetComponentErrors() {
		groups.get(isViewMode()).getFields().forEach(f -> {
			((AbstractComponent) f).setComponentError(null);
		});
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

	public void setAfterUploadConsumer(BiConsumer<String, byte[]> afterUploadConsumer) {
		this.afterUploadConsumer = afterUploadConsumer;
	}

	/**
	 * Shows/hides an attribute group
	 *
	 * @param key     the message key by which the group is identified
	 * @param visible whether to show/hide the group
	 */
	public void setAttributeGroupVisible(String key, boolean visible) {
		Object c = attributeGroups.get(false).get(key);
		setGroupVisible(c, visible);
		c = attributeGroups.get(true).get(key);
		setGroupVisible(c, visible);
	}

	/**
	 * Shows or hides the component for a certain property - this will work*
	 * regardless of the view
	 *
	 * @param propertyName the name of the property for which to show/hide the
	 *                     property
	 * @param visible
	 */
	public void setComponentVisible(String propertyName, boolean visible) {
		setLabelVisible(propertyName, visible);
		AbstractComponent field = getField(isViewMode(), propertyName);
		if (field != null) {
			field.setVisible(visible);
		}
	}

	public void setCustomSaveConsumer(Consumer<T> customSaveConsumer) {
		this.customSaveConsumer = customSaveConsumer;
	}

	/**
	 * Sets the default value for a field
	 * 
	 * @param field the field
	 * @param value the default value
	 */
	private <R> void setDefaultValue(HasValue<R> field, R value) {
		field.setValue(value);
	}

	public void setDetailJoins(FetchJoinInformation[] detailJoins) {
		this.detailJoins = detailJoins;
	}

	public void setEntity(T entity) {
		setEntity(entity, entity.getId() != null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void refreshFieldFilters() {

		// if there is a field filter, make sure it is used
		for (String propertyName : getFieldFilters().keySet()) {
			Optional<Binding<T, ?>> binding = groups.get(isViewMode()).getBinding(propertyName);
			if (binding.isPresent()) {
				HasValue<?> field = binding.get().getField();
				if (field instanceof CustomEntityField) {
					SerializablePredicate<?> fieldFilter = getFieldFilters().get(propertyName);
					((CustomEntityField) field).refresh(fieldFilter);
				}
			}
		}

		// otherwise refresh
		groups.get(isViewMode()).getFields().forEach(field -> {
			if (field instanceof Refreshable && !(field instanceof CustomEntityField)) {
				((Refreshable) field).refresh();
			}
		});
	}

	private void setEntity(T entity, boolean checkIterationButtons) {
		this.entity = entity;

		refreshFieldFilters();

		// inform all children
		for (CanAssignEntity<ID, T> field : assignEntityToFields) {
			field.assignEntity(entity);
		}
		afterEntitySet(this.entity);

		setViewMode(getFormOptions().isOpenInViewMode() && entity.getId() != null, checkIterationButtons);

		// recreate the group
		groups.get(isViewMode()).setBean(entity);

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

	/**
	 * Sets the "required" status for a field. Convenience method that also
	 * correctly handles the situation in which there are multiple fields behind
	 * each other on the same row
	 *
	 * @param propertyName the name of the property
	 * @param required     whether the property is required
	 */
	public void setFieldRequired(String propertyName, boolean required) {
		AbstractComponent field = getField(isViewMode(), propertyName);
		if (field != null) {

			if (!required) {
				((AbstractComponent) field).setComponentError(null);
			}

			// if there are multiple fields in a row, we need to some additional trickery
			// since
			// to make sure the "required" asterisk is properly displayed
			AttributeModel am = getEntityModel().getAttributeModel(propertyName);
			if (am != null && !am.getGroupTogetherWith().isEmpty()) {
				HorizontalLayout hz = (HorizontalLayout) field.getParent().getParent();
				if (required) {
					hz.addStyleName(DynamoConstants.CSS_REQUIRED);
				} else {
					hz.removeStyleName(DynamoConstants.CSS_REQUIRED);
				}
			}
		}
	}

	public void setFormTitleWidth(Integer formTitleWidth) {
		this.formTitleWidth = formTitleWidth;
	}

	/**
	 * Hides/shows a group of components
	 *
	 * @param c       the parent component of the group
	 * @param visible whether to set the component to visible
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
	 * @param propertyName the name of the property for which to show/hide the label
	 * @param visible      whether to show the label
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

	public void setNestedMode(boolean nestedMode) {
		this.nestedMode = nestedMode;
	}

	public void setSupportsIteration(boolean supportsIteration) {
		this.supportsIteration = supportsIteration;
	}

	/**
	 * Overwrite the value of the title label
	 *
	 * @param value the desired value
	 */
	public void setTitleLabel(String value) {
		titleLabels.get(isViewMode()).setValue(value);
	}

	/**
	 * Sets the view mode of the component
	 * 
	 * @param viewMode the desired view mode
	 */
	public void setViewMode(boolean viewMode) {
		setViewMode(viewMode, true);
	}

	/**
	 * Switches the form from or to view mode
	 *
	 * @param viewMode the new view mode
	 */
	private void setViewMode(boolean viewMode, boolean checkIterationButtons) {
		boolean oldMode = this.viewMode;

		// check what the new view mode must become and adapt the screen
		this.viewMode = !isEditAllowed() || viewMode;

		groups.get(isViewMode()).setBean(entity);

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

		resetComponentErrors();
		if (oldMode != this.viewMode) {
			afterModeChanged(isViewMode());
			groups.get(isViewMode()).getFields().filter(f -> f instanceof DetailsEditLayout<?, ?>)
					.map(f -> (DetailsEditLayout<?, ?>) f).forEach(f -> f.signalModeChange(isViewMode()));
		}
	}

	/**
	 * Apply styling to a label
	 *
	 * @param propertyName the name of the property
	 * @param className    the name of the CSS class to add
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

	/**
	 * Validates all fields and returns true if an error occurs
	 *
	 * @return
	 */
	@Override
	public boolean validateAllFields() {
		boolean error = false;

		BinderValidationStatus<T> status = groups.get(isViewMode()).validate();
		error = !status.isOk();

		// validate nested form and components
		error |= groups.get(isViewMode()).getFields().anyMatch(f -> {
			if (f instanceof NestedComponent) {
				return ((NestedComponent) f).validateAllFields();
			}
			return false;
		});
		return error;
	}

}
