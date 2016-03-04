package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.table.BaseTableWrapper;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Base class for a layout that contains both a results table and a details
 * view. Based on the screen mode these can be displayed either next to each
 * other or below each other
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class BaseSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCollectionLayout<ID, T> implements Reloadable {

	private static final long serialVersionUID = 4606800218149558500L;

	// the form layout that is nested inside the detail view
	private Layout detailFormLayout;

	// the outer layout that holds the detail view
	private Layout detailLayout;

	private ModelBasedEditForm<ID, T> editForm;

	private TextField extraSearchField;

	private Map<String, Filter> fieldFilters = new HashMap<>();

	private Component headerLayout;

	private VerticalLayout main;

	private Component selectedDetailLayout;

	private BaseTableWrapper<ID, T> tableWrapper;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service used to query the database
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 * @param sortOrder
	 *            the sort order
	 * @param joins
	 *            the joins used to query the database
	 */
	public BaseSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
	        FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
	}

	/**
	 * Callback method that is called after a detail entity has been selected
	 * 
	 * @param editForm
	 *            the edit form that contains the entity
	 * @param entity
	 *            the selected entity
	 */
	protected void afterDetailSelected(ModelBasedEditForm<ID, T> editForm, T entity) {
		// override in subclass
	}

	/**
	 * Perform any actions after the screen reloads after a save. This is
	 * usually used to reselect the item that was selected before
	 * 
	 * @param t
	 */
	protected abstract void afterReload(T t);

	@Override
	public void attach() {
		super.attach();
		init();
		build();
	}

	/**
	 * Builds the form
	 */
	@Override
	public void build() {
		main = new DefaultVerticalLayout(true, true);

		HorizontalSplitPanel splitter = null;
		VerticalLayout splitterLayout = null;

		detailLayout = new DefaultVerticalLayout();
		emptyDetailView();

		// optional header
		headerLayout = constructHeaderLayout();
		if (headerLayout != null) {
			main.addComponent(headerLayout);
		}

		// additional quick search field
		if (!isHorizontalMode()) {
			extraSearchField = constructSearchField();
			if (extraSearchField != null) {
				main.addComponent(extraSearchField);
			}
		}

		// constructs the results table
		constructTable();

		// extra splitter (for horizontal mode)
		if (isHorizontalMode()) {
			splitter = new HorizontalSplitPanel();
			main.addComponent(splitter);

			splitterLayout = new DefaultVerticalLayout(false, true);
			extraSearchField = constructSearchField();
			if (extraSearchField != null) {
				splitterLayout.addComponent(extraSearchField);
			}

			splitterLayout.addComponent(tableWrapper);
			splitter.setFirstComponent(splitterLayout);
		} else {
			main.addComponent(tableWrapper);
		}

		// button bar
		setButtonBar(new DefaultHorizontalLayout());

		if (isHorizontalMode()) {
			splitterLayout.addComponent(getButtonBar());
		} else {
			main.addComponent(getButtonBar());
		}

		// create a panel to hold the edit form
		Panel editPanel = new Panel();
		editPanel.setContent(detailLayout);

		if (isHorizontalMode()) {
			// create the layout that is the right part of the splitter
			VerticalLayout extra = new DefaultVerticalLayout(true, false);
			extra.addComponent(editPanel);
			splitter.setSecondComponent(extra);
		} else {
			main.addComponent(editPanel);
		}

		// only add the "Add" button when it makes sense in this page
		if (!getFormOptions().isHideAddButton() && isEditAllowed()) {
			Button addButton = new Button(message("ocs.add"));
			addButton.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					setSelectedItem(createEntity());
					detailsView(getSelectedItem());
				}
			});
			getButtonBar().addComponent(addButton);
		}

		if (getFormOptions().isShowRemoveButton() && isEditAllowed()) {
			constructRemoveButton();
		}

		// allow the user to define extra buttons
		postProcessButtonBar(getButtonBar());

		postProcessLayout(main);

		checkButtonState(null);
		setCompositionRoot(main);
	}

	/**
	 * Constructs a header layout (displayed above the actual tabular content)
	 * 
	 * @return
	 */
	protected Component constructHeaderLayout() {
		return null;
	}

	/**
	 * Constructs the remove button
	 */
	protected void constructRemoveButton() {
		Button removeButton = new RemoveButton() {

			@Override
			protected void doDelete() {
				getService().delete(getSelectedItem());
				setSelectedItem(null);
				emptyDetailView();
				reload();
			}
		};
		getButtonBar().addComponent(removeButton);
		registerDetailButton(removeButton);
	}

	/**
	 * Constructs an extra quick search field - delegate to subclasses for
	 * implementation
	 * 
	 * @param parent
	 */
	protected abstract TextField constructSearchField();

	/**
	 * Constructs the table used for displaying the data
	 * 
	 * @return
	 */
	protected abstract void constructTable();

	/**
	 * Fills the detail part of the screen with a custom component
	 * 
	 * @param component
	 */
	protected void customDetailView(Component component) {
		detailLayout.replaceComponent(selectedDetailLayout, component);
		selectedDetailLayout = component;
	}

	/**
	 * Shows the details of a selected entity
	 * 
	 * @param parent
	 *            the parent of the entity
	 * @param entity
	 *            the entity itself
	 */
	protected void detailsView(T entity) {

		if (detailFormLayout == null) {
			detailFormLayout = new DefaultVerticalLayout(false, false);

			// canceling is not needed in the inline view
			getFormOptions().setHideCancelButton(true);

			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(),
			        getFormOptions(), fieldFilters) {

				@Override
				protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
					// update the selected item so master and detail are in sync
			        // again
					setSelectedItem(entity);
					reload();
					afterReload(entity);
				}

				@Override
				protected Field<?> constructCustomField(EntityModel<T> entityModel,
			            AttributeModel attributeModel, boolean viewMode) {
					return BaseSplitLayout.this.constructCustomField(entityModel, attributeModel,
			                viewMode, false);
				}

				@Override
				protected boolean isEditAllowed() {
					return BaseSplitLayout.this.isEditAllowed();
				}

				@Override
				protected void postProcessEditFields() {
					BaseSplitLayout.this.postProcessEditFields(editForm);
				}
			};
			editForm.build();

			detailFormLayout.addComponent(editForm);
		} else {
			// reset the form's view mode if needed
			editForm.setViewMode(getFormOptions().isOpenInViewMode());
			editForm.setEntity(entity);
		}
		afterDetailSelected(editForm, entity);

		detailLayout.replaceComponent(selectedDetailLayout, detailFormLayout);
		selectedDetailLayout = detailFormLayout;
	}

	/**
	 * Clears the detail view
	 */
	protected void emptyDetailView() {
		VerticalLayout vLayout = new VerticalLayout();
		vLayout.addComponent(new Label(message("ocs.inline.select.item")));

		detailLayout.replaceComponent(selectedDetailLayout, vLayout);
		selectedDetailLayout = vLayout;
	}

	public Layout getDetailLayout() {
		return detailLayout;
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	public Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	public BaseTableWrapper<ID, T> getTableWrapper() {
		return tableWrapper;
	}

	/**
	 * Perform any required initialization (e.g. load the required items) before
	 * attaching the screen
	 */
	protected abstract void init();

	/**
	 * Indicates whether the panel is in horizontal mode
	 * 
	 * @return
	 */
	protected boolean isHorizontalMode() {
		return ScreenMode.HORIZONTAL.equals(getFormOptions().getScreenMode());
	}

	/**
	 * Post processes the edit fields. This method is called once, just before
	 * the screen is displayed in edit mode for the first time
	 * 
	 * @param editForm
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// do nothing by default - override in subclasses
	}

	@Override
	public void reload() {
		// replace the header layout (if there is one)
		Component component = constructHeaderLayout();
		if (component != null) {
			if (headerLayout != null) {
				main.replaceComponent(headerLayout, component);
			} else {
				main.addComponent(component, 0);
			}
		} else if (headerLayout != null) {
			main.removeComponent(headerLayout);
		}
		headerLayout = component;

		if (extraSearchField != null) {
			extraSearchField.setValue("");
		}

		// refresh the details
		if (getSelectedItem() != null) {
			detailsView(getSelectedItem());
		}
	}

	/**
	 * Reloads the details view only
	 */
	public void reloadDetails() {
		this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getJoins()));
		detailsView(getSelectedItem());
		getTableWrapper().reloadContainer();
	}

	public void setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setTableWrapper(BaseTableWrapper<ID, T> tableWrapper) {
		this.tableWrapper = tableWrapper;
	}

	public void setViewMode(boolean viewMode) {
		if (getSelectedItem() != null) {
			editForm.setViewMode(viewMode);
		}
	}

}
