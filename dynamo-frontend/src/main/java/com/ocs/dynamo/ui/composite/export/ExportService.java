package com.ocs.dynamo.ui.composite.export;

import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;

/**
 * Service for exporting table contents to XLSX or CSV
 * @author Bas Rutten
 *
 */
public interface ExportService {

	/**
	 * 
	 * @param clazz
	 * @param entityModel
	 * @param predicate
	 * @param sortOrders
	 * @param joins
	 * @return
	 */
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] export(Class<T> clazz,
			EntityModel<T> entityModel, SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders,
			FetchJoinInformation... joins);
}
