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

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;

/**
 * Utilities for dealing with the grid
 * @author Bas Rutten
 *
 */
public final class GridUtils {

	private GridUtils() {
		// hidden constructor
	}

	/**
	 * Perform the default initialization for a table
	 * 
	 * @param table
	 *            the table to initialize
	 */
	public static <T> void defaultInitialization(Grid<T> grid) {
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.setSelectionMode(SelectionMode.SINGLE);
	}

	/**
	 * Returns the currency symbol to be used in a certain grid. This will return
	 * the custom currency symbol for a certain table, or the default currency
	 * symbol if no custom currency symbol is set
	 * 
	 * @param table
	 *            the table
	 * @return
	 */
	public static <T> String getCurrencySymbol(Grid<T> grid) {
		String cs = null;
		if (grid instanceof ModelBasedGrid) {
			cs = ((ModelBasedGrid<?, ?>) grid).getCurrencySymbol();
		}
		// } else if (table instanceof ModelBasedTreeTable) {
		// cs = ((ModelBasedTreeTable<?, ?>) table).getCurrencySymbol();
		// }
		if (cs == null) {
			cs = VaadinUtils.getCurrencySymbol();
		}
		return cs;
	}

}
