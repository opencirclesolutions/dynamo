package com.ocs.dynamo.ui.composite.export;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.UI;

/**
 * Delegate for handling exports. This service is responsibly for displaying an
 * export dialog and performing the actual export as well
 * 
 * @author Bas Rutten
 *
 */
public interface ExportDelegate {

	/**
	 * Exports a variable set of data
	 * 
	 * @param ui the Vaadin UI
	 * @param entityModel
	 * @param mode
	 * @param predicate
	 * @param sortOrders
	 * @param joins
	 */
	<ID extends Serializable, T extends AbstractEntity<ID>> void export(UI ui, EntityModel<T> entityModel,
			ExportMode mode, SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders,
			FetchJoinInformation... joins);

	/**
	 * Exports a fixed set of data
	 * 
	 * @param ui the Vaadin UI
	 * @param entityModel the entity model
	 * @param mode        the export mode
	 * @param items       the entities to export
	 */
	<ID extends Serializable, T extends AbstractEntity<ID>> void exportFixed(UI ui, EntityModel<T> entityModel,
			ExportMode mode, Collection<T> items);

}
