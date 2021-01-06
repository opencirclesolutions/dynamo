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
package com.ocs.dynamo.ui.composite.grid;

import java.util.List;
import java.util.stream.Collectors;

import com.ocs.dynamo.domain.model.EntityModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortOrder;

/**
 * 
 * @author Bas Rutten
 *
 */
public class SortOrderUtil {

	/**
	 * Translates a list of sort orders from GridSortOrder to SortOrder
	 * 
	 * @param <T>
	 * @param entityModel
	 * @param gridSort
	 * @return
	 */
	public static <T> List<SortOrder<?>> restoreSortOrder(EntityModel<T> entityModel, List<GridSortOrder<T>> gridSort) {
		List<SortOrder<?>> collect = gridSort.stream().map(s -> {
			String prop = s.getSorted().getKey();
			return new SortOrder<>(prop, s.getDirection());
		}).collect(Collectors.toList());
		return collect;
	}
}
