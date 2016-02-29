package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.And;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.TextField;

/**
 * A split layout - contains both a table and a details view - that uses a
 * service to fetch data
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class ServiceBasedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseSplitLayout<ID, T> {

	private static final long serialVersionUID = 1068860513192819804L;

	private Filter filter;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param formOptions
	 * @param fieldFilters
	 * @param sortOrder
	 * @param joins
	 */
	public ServiceBasedSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
			FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
	}

	@Override
	public void reload() {
		super.reload();
		getTableWrapper().reloadContainer();
	}

	@Override
	protected void init() {
		filter = createFilter();
	}

	/**
	 * Creates the search filter
	 * 
	 * @return
	 */
	protected abstract Filter createFilter();

	@Override
	protected void constructTable() {
		ServiceResultsTableWrapper<ID, T> tw = new ServiceResultsTableWrapper<ID, T>(getService(),
				getEntityModel(), QueryType.ID_BASED, filter, getSortOrder(), getJoins()) {

			@Override
			protected void onSelect(Object selected) {
				setSelectedItems(selected);
				checkButtonState(getSelectedItem());
				if (getSelectedItem() != null) {
					detailsView(getSelectedItem());
				}
			}
		};
		tw.build();
		setTableWrapper(tw);

	}

	@SuppressWarnings("unchecked")
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items
				Collection<?> col = (Collection<?>) selectedItems;
				ID id = (ID) col.iterator().next();
				setSelectedItem(getService().fetchById(id));
			} else {
				ID id = (ID) selectedItems;
				setSelectedItem(getService().fetchById(id));
			}
		} else {
			setSelectedItem(null);
			emptyDetailView();
		}
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * Constructs a quick search field
	 */
	@Override
	protected TextField constructSearchField() {
		if (getFormOptions().isShowExtraSearchField()) {
			TextField searchField = new TextField(message("ocs.search"));

			// respond to the user entering a search term
			searchField.addTextChangeListener(new TextChangeListener() {

				@Override
				public void textChange(TextChangeEvent event) {
					String text = event.getText();
					if (!StringUtils.isEmpty(text)) {
						Filter extra = constructExtraSearchFilter(text);

						Filter f = extra;
						if (getFilter() != null) {
							f = new And(extra, getFilter());
						}
						getContainer().search(f);
					} else {
						getContainer().search(filter);
					}
				}
			});
			return searchField;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected ServiceContainer<ID, T> getContainer() {
		return (ServiceContainer<ID, T>) getTableWrapper().getContainer();
	}

	/**
	 * Constructs the extra search filter - override in subclass if your panel
	 * contains a quick search field
	 * 
	 * @param value
	 * @return
	 */
	protected Filter constructExtraSearchFilter(String value) {
		return null;
	}

	@Override
	protected void afterReload(T t) {
		// in a lazy query container, the entity ID is used as the key
		getTableWrapper().getTable().select(t == null ? null : t.getId());
	}
}
