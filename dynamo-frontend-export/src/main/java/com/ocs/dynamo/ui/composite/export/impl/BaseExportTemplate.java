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
package com.ocs.dynamo.ui.composite.export.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.domain.query.FixedDataSetIterator;
import com.ocs.dynamo.domain.query.PagingDataSetIterator;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.type.ExportMode;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Base class for entity model based exports to Excel or CSV
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T>  the type of the entity to export
 */
public abstract class BaseExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> {

	protected static final int PAGE_SIZE = 1000;

	@Getter(AccessLevel.PROTECTED)
	private final EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator()
			.getEntityModelFactory();

	@Getter(AccessLevel.PROTECTED)
	private final Filter filter;

	@Getter(AccessLevel.PROTECTED)
	private final FetchJoinInformation[] joins;

	@Getter(AccessLevel.PROTECTED)
	private final BaseService<ID, T> service;

	@Getter(AccessLevel.PROTECTED)
	private final SortOrder[] sortOrders;

	@Getter(AccessLevel.PROTECTED)
	private final String title;

	@Getter(AccessLevel.PROTECTED)
	private final EntityModel<T> entityModel;

	@Getter(AccessLevel.PROTECTED)
	private final ExportMode exportMode;

	/**
	 * Constructor
	 * 
	 * @param service     the service used for contacting the database
	 * @param entityModel the entity model of the entity to export
	 * @param exportMode  the desired export mode
	 * @param sortOrders  the sort orders
	 * @param filter      the filter used to restrict the result set
	 * @param title       the title of the sheet
	 * @param joins       the joins to use when retrieving data
	 */
	protected BaseExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
			SortOrder[] sortOrders, Filter filter, String title, FetchJoinInformation... joins) {
		this.service = service;
		this.exportMode = exportMode;
		this.entityModel = entityModel;
		// if no sort order specified, then sort by id descending
		this.sortOrders = sortOrders != null ? sortOrders
				: new SortOrder[] { new SortOrder(DynamoConstants.ID, Direction.DESC) };
		this.filter = filter;
		this.title = title;
		this.joins = joins;
	}

	/**
	 * Generates the content to export
	 *
	 * @param iterator data set iterator that contains the rows to include
	 * @return the byte representation of the exported data
	 * @throws IOException when the data cannot be written
	 */
	protected abstract byte[] generate(DataSetIterator<ID, T> iterator) throws IOException;

	/**
	 * Check whether a certain attribute model must be included in the export
	 *
	 * @param am the attribute model
	 * @return <code>true</code> if the attribute must be included,
	 *         <code>false</code> otherwise
	 */
	protected boolean mustShow(AttributeModel am) {
		boolean visible = ExportMode.FULL.equals(exportMode) ? am.isVisible() : am.isVisibleInGrid();
		// never show invisible or LOB attributes
		return visible && !AttributeType.LOB.equals(am.getAttributeType());
	}

	/**
	 * Carries out the export
	 * 
	 * @return the byte representation of the export
	 */
	public final byte[] process() {
		try {
			// retrieve all store series based on the IDs
			List<ID> ids = service.findIds(getFilter(), sortOrders);
			PagingDataSetIterator<ID, T> iterator = new PagingDataSetIterator<>(ids,
					page -> service.fetchByIds(page, new SortOrders(sortOrders), joins), PAGE_SIZE);
			return generate(iterator);
		} catch (IOException ex) {
			throw new OCSRuntimeException(ex.getMessage(), ex);
		}
	}

	/**
	 * Processes a fixed set of data
	 * 
	 * @param items the set of data to process
	 * @return the byte representation
	 */
	public final byte[] processFixed(List<T> items) {
		try {
			// retrieve all store series based on the IDs
			FixedDataSetIterator<ID, T> iterator = new FixedDataSetIterator<>(items);
			return generate(iterator);
		} catch (IOException ex) {
			throw new OCSRuntimeException(ex.getMessage(), ex);
		}
	}

}
