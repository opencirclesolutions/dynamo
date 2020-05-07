package com.ocs.dynamo.functional.ui;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.functional.domain.AbstractEntityTranslated;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.EntityComboBox;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
//import com.vaadin.data.provider.ListDataProvider;
//import com.vaadin.data.provider.SortOrder;
//import com.vaadin.server.SerializablePredicate;

/**
 * A combo box for entities that support multiple translations
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 * @param <E> the type of the translation
 */
public class TranslationComboBox<ID extends Serializable, T extends AbstractEntityTranslated<ID, E>, E extends Translation<T>>
		extends EntityComboBox<ID, T> {

	private static final long serialVersionUID = 4403245109020319295L;

	/**
	 * Constructor
	 * 
	 * @param targetEntityModel the entity model of the encapsulating entry
	 * @param attributeModel    the attribute model
	 * @param service           the service
	 * @param mode              the selection mode (single)
	 * @param filter            the filter to apply to the search results
	 * @param sharedProvider    shared data provider
	 * @param items             the items
	 * @param sortOrders        list of sort orders to apply
	 */
	public TranslationComboBox(EntityModel<T> targetEntityModel, AttributeModel attributeModel,
							   BaseService<ID, T> service, SelectMode mode, SerializablePredicate<T> filter,
							   ListDataProvider<T> sharedProvider, List<T> items, SortOrder<?>... sortOrders) {
		super(targetEntityModel, attributeModel, service, mode, filter, sharedProvider, items, sortOrders);
	}

	@Override
	public void refresh(SerializablePredicate<T> filter) {
		updateLocale();
		super.refresh(filter);
	}

	@Override
	public void refresh() {
		updateLocale();
		super.refresh();
	}

	/**
	 * Updates the locale settings
	 */
	private void updateLocale() {
		Locale locale = VaadinUtils.getLocale();
		com.ocs.dynamo.functional.domain.Locale loc = new com.ocs.dynamo.functional.domain.Locale();
		loc.setCode(locale.toString());
		this.setItemLabelGenerator(t -> {
			E e = t.getTranslation(getAttributeModel().getPath(), loc);
			return e == null ? null : e.getTranslation();
		});
	}

}
