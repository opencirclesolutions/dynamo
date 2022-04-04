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

import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortOrder;

import lombok.experimental.UtilityClass;

/**
 * 
 * @author Bas Rutten
 *
 */
@UtilityClass
public final class SortOrderUtil {

	/**
	 * Translates a list of grid sort orders to a list of SortOrders
	 * @param <T>
	 * @param gridSort
	 * @return
	 */
	public static <T> List<SortOrder<?>> restoreSortOrder(List<GridSortOrder<T>> gridSort) {
		return gridSort.stream().map(s -> {
			String prop = s.getSorted().getKey();
			return new SortOrder<>(prop, s.getDirection());
		}).collect(Collectors.toList());
	}
}
