package com.ocs.dynamo.ui.composite.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.filter.listener.FilterChangeEvent;
import com.ocs.dynamo.filter.listener.FilterListener;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * A search form that is constructed based on the metadata model
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity to search for
 */
public class ModelBasedSearchForm<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCustomComponent implements FilterListener, Button.ClickListener {

	// the types of search field
	protected enum FilterType {
		BETWEEN, BOOLEAN, ENTITY, ENUM, LIKE
	}

	private static final long serialVersionUID = -7226808613882934559L;

	// list of additional filters (these will be included in every query, even
	// if the search form is empty)
	private List<Filter> additionalFilters = new ArrayList<>();

	// button to clear the search form
	private Button clearButton;

	// the currently active filters
	private List<Filter> currentFilters = new ArrayList<Filter>();

	// the entity model on which to base the search form
	private EntityModel<T> entityModel;

	// custom field factory
	private ModelBasedFieldFactory<T> fieldFactory;

	// list of extra filters to apply to certain fields
	private Map<String, Filter> fieldFilters = new HashMap<>();

	// the layout that contains all the filter fields
	private Component filterLayout;

	private FormOptions formOptions;

	// the list of filter groups
	private Map<String, FilterGroup> groups = new HashMap<>();

	// the object to search when the button is pressed
	private Searchable searchable;

	// the search button
	private Button searchButton;

	// toggle butoon (hides/shows the search form)
	private Button toggleButton;

	/**
	 * Constructor
	 * 
	 * @param searchable
	 *            the component on which to carry out the search
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 */
	public ModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel,
	        FormOptions formOptions) {
		this(searchable, entityModel, formOptions, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param searchable
	 *            the component on which to carry out the search
	 * @param entityModel
	 *            the entity model
	 * @param formOptions
	 *            the form options
	 * @param additionalFilters
	 *            the additional filters to apply to every search action
	 * @param fieldFilters
	 *            a map of filters to apply to the individual fields
	 */
	public ModelBasedSearchForm(Searchable searchable, EntityModel<T> entityModel,
	        FormOptions formOptions, List<Filter> additionalFilters,
	        Map<String, Filter> fieldFilters) {
		this.searchable = searchable;
		this.fieldFactory = ModelBasedFieldFactory.getSearchInstance(entityModel,
		        getMessageService());
		this.formOptions = formOptions;
		this.additionalFilters = additionalFilters == null ? new ArrayList<Filter>()
		        : additionalFilters;
		this.currentFilters.addAll(this.additionalFilters);
		this.fieldFilters = fieldFilters == null ? new HashMap<String, Filter>() : fieldFilters;
		this.entityModel = entityModel;
	}

	/**
	 * Callback method that is called when the user toggles the visibility of
	 * the search form
	 * 
	 * @param visible
	 */
	protected void afterSearchFieldToggle(boolean visible) {
		// override in subclasses
	}

	@Override
	public void build() {
		VerticalLayout main = new VerticalLayout();

		// create the search form
		filterLayout = constructFilterLayout(entityModel);
		main.addComponent(filterLayout);

		// create the button bar
		HorizontalLayout buttonBar = new DefaultHorizontalLayout();
		main.addComponent(buttonBar);

		// create the search button
		searchButton = new Button(message("ocs.search"));
		searchButton.setImmediate(true);
		searchButton.addClickListener(this);
		buttonBar.addComponent(searchButton);

		// create the clear button
		clearButton = new Button();
		clearButton.setCaption(message("ocs.clear"));
		clearButton.setImmediate(true);
		clearButton.addClickListener(this);
		clearButton.setVisible(!formOptions.isHideClearButton());
		buttonBar.addComponent(clearButton);

		// create the toggle button
		toggleButton = new Button(message("ocs.hide"));
		toggleButton.addClickListener(this);
		toggleButton.setVisible(formOptions.isShowToggleButton());
		buttonBar.addComponent(toggleButton);

		postProcessButtonBar(buttonBar);

		// initial search
		if (!currentFilters.isEmpty()) {
			Filter composite = new And(currentFilters.toArray(new Filter[0]));
			searchable.search(composite);
		}
		postProcessFilterGroups(groups);

		setCompositionRoot(main);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == searchButton) {
			search();
		} else if (event.getButton() == clearButton) {
			clear();
			search();
		} else if (event.getButton() == toggleButton) {
			boolean visible = filterLayout.isVisible();
			if (visible) {
				toggleButton.setCaption(message("ocs.show.search.fields"));
			} else {
				toggleButton.setCaption(message("ocs.hide.search.fields"));
			}
			filterLayout.setVisible(!visible);
			afterSearchFieldToggle(filterLayout.isVisible());
		}
	}

	/**
	 * Clears all filters then performs a fresh search
	 */
	protected void clear() {
		// Clear all filter groups
		for (FilterGroup group : groups.values()) {
			group.reset();
		}
		currentFilters.clear();
		currentFilters.addAll(additionalFilters);
	}

	/**
	 * Creates a custom field - override in subclasses if needed
	 * 
	 * @param entityModel
	 *            the entity model of the entity to search for
	 * @param attributeModel
	 *            the attribute model the attribute model of the property that
	 *            is bound to the field
	 * @return
	 */
	protected Field<?> constructCustomField(EntityModel<T> entityModel,
	        AttributeModel attributeModel) {
		return null;
	}

	/**
	 * Creates a search field based on an attribute model
	 * 
	 * @param entityModel
	 *            the entity model of the entity to search for
	 * @param attributeModel
	 *            the attribute model the attribute model of the property that
	 *            is bound to the field
	 * @return
	 */
	protected Field<?> constructField(EntityModel<T> entityModel, AttributeModel attributeModel) {

		Field<?> field = constructCustomField(entityModel, attributeModel);
		if (field == null) {
			field = fieldFactory.constructField(attributeModel, fieldFilters);
		}

		if (field != null) {
			field.setSizeFull();
		}
		return field;
	}

	/**
	 * Constructs a filter group
	 * 
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @return
	 */
	protected FilterGroup constructFilterGroup(EntityModel<T> entityModel,
	        AttributeModel attributeModel) {
		Field<?> field = this.constructField(entityModel, attributeModel);
		if (field != null) {
			FilterType filterType = FilterType.BETWEEN;
			if (String.class.isAssignableFrom(attributeModel.getType())) {
				filterType = FilterType.LIKE;
			} else if (Boolean.class.isAssignableFrom(attributeModel.getType())
			        || Boolean.TYPE.isAssignableFrom(attributeModel.getType())) {
				filterType = FilterType.BOOLEAN;
			} else if (attributeModel.getType().isEnum()) {
				filterType = FilterType.ENUM;
			} else if (AbstractEntity.class.isAssignableFrom(attributeModel.getType())) {
				filterType = FilterType.ENTITY;
			}

			Component comp = field;
			Field<?> auxField = null;
			if (FilterType.BETWEEN.equals(filterType)) {
				// in case of a between value, construct two fields for the
				// lower
				// and upper bounds
				String from = message("ocs.from");
				field.setCaption(attributeModel.getDisplayName() + " " + from);
				auxField = constructField(entityModel, attributeModel);
				String to = message("ocs.to");
				auxField.setCaption(attributeModel.getDisplayName() + " " + to);
				auxField.setVisible(true);
				HorizontalLayout layout = new DefaultHorizontalLayout();
				layout.setSizeFull();
				layout.addComponent(field);
				layout.addComponent(auxField);
				comp = layout;
			}
			return new FilterGroup(attributeModel, attributeModel.getPath(), filterType, comp,
			        field, auxField);
		}
		return null;
	}

	/**
	 * Builds the layout that contains the various search filters
	 * 
	 * @param entityModel
	 *            the entity model
	 * @return
	 */
	protected Component constructFilterLayout(EntityModel<T> entityModel) {
		FormLayout form = new FormLayout();

		if (!formOptions.isPopup()) {
			form.setStyleName(OCSConstants.CSS_CLASS_HALFSCREEN);
		}

		// iterate over the searchable attributes and add a field for each
		iterate(form, entityModel.getAttributeModels());
		return form;
	}

	@Override
	public void onFilterChange(FilterChangeEvent event) {
		currentFilters.remove(event.getOldFilter());
		if (event.getNewFilter() != null) {
			currentFilters.add(event.getNewFilter());
		}
	}

	/**
	 * Programmatically force a search
	 * 
	 * @param propertyId
	 *            the property ID to search on
	 * @param value
	 *            the value of the property
	 */
	public void forceSearch(String propertyId, Object value) {
		setSearchValue(propertyId, value);
		search();
	}

	public Map<String, FilterGroup> getGroups() {
		return groups;
	}

	/**
	 * Recursively iterate over the attribute models (including nested models)
	 * and add search fields if the fields are searchable
	 * 
	 * @param form
	 *            the form to add the search fields to
	 * @param attributeModels
	 *            the attribute models to iterate over
	 */
	private void iterate(Layout form, List<AttributeModel> attributeModels) {
		for (AttributeModel attributeModel : attributeModels) {
			if (attributeModel.isSearchable()
			        && !AttributeType.DETAIL.equals(attributeModel.getAttributeType())) {

				FilterGroup group = constructFilterGroup(entityModel, attributeModel);
				group.getFilterComponent().setSizeFull();
				form.addComponent(group.getFilterComponent());

				// register with the form and set the listener
				group.addListener(this);
				groups.put(group.getPropertyId(), group);
			}

			// also support search on nested attributes
			if (attributeModel.getNestedEntityModel() != null) {
				EntityModel<?> nested = attributeModel.getNestedEntityModel();
				iterate(form, nested.getAttributeModels());
			}
		}
	}

	protected void postProcessButtonBar(Layout buttonBar) {
		// Use in subclass to add additional buttons
	}

	protected void postProcessFilterGroups(Map<String, FilterGroup> groups) {
		// overwrite in subclasses
	}

	/**
	 * Trigger the actual search
	 */
	public void search() {
		if (!currentFilters.isEmpty()) {
			Filter composite = new And(currentFilters.toArray(new Filter[0]));
			searchable.search(composite);
		} else {
			// search without any filters
			searchable.search(null);
		}
	}

	/**
	 * Manually set the value for a certain search field
	 * 
	 * @param propertyId
	 *            the ID of the property
	 * @param value
	 *            the desired value
	 */
	public void setSearchValue(String propertyId, Object value) {
		setSearchValue(propertyId, value, null);
	}

	/**
	 * Manually set the value for a certain search field
	 * 
	 * @param propertyId
	 *            the ID of the property
	 * @param value
	 *            the desired value for the main field
	 * @param auxValue
	 *            the desired value for the auxiliary field
	 */
	public void setSearchValue(String propertyId, Object value, Object auxValue) {
		FilterGroup group = groups.get(propertyId);
		group.getField().setValue((Object) value);
		if (group.getAuxField() != null) {
			group.getAuxField().setValue(auxValue);
		}
	}

}
