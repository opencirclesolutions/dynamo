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
 * 
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
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] export(EntityModel<T> entityModel,
			SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders, FetchJoinInformation... joins);
}
