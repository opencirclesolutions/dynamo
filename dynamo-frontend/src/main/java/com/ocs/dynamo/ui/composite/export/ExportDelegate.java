package com.ocs.dynamo.ui.composite.export;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Delegate for handling exports. This service is responsibly for displaying an
 * export dialog and performing the actual export as well
 * 
 * @author Bas Rutten
 *
 */
public interface ExportDelegate {

    /**
     * Exports a non-fixed set of data (e.g. the data in a grid)
     * 
     * @param entityModel the entity model of the entity that is being exported
     * @param mode        the export mode
     * @param predicate   filter predicate to limit the results
     * @param sortOrders  sort orders to used to order the results
     * @param joins       the fetch joins to apply when fetching data
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> void export(EntityModel<T> entityModel, ExportMode mode,
            SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders, FetchJoinInformation... joins);

    /**
     * Exports a fixed set of data
     * 
     * @param entityModel the entity model
     * @param mode        the export mode
     * @param items       the entities to export
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> void exportFixed(EntityModel<T> entityModel, ExportMode mode,
            Collection<T> items);

}
