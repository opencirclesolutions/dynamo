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
package com.ocs.dynamo.ui.composite.table;

import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.ui.Grid;

/**
 * Several table related functions to reuse on both Table and TreeTable
 * subclasses.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
public final class TableUtils {

	private TableUtils() {
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
		// grid.setImmediate(true);
		// grid.setRea
		// grid.setMultiSelect(false);
		// grid.setSelectable(true);
		// grid.setColumnCollapsingAllowed(true);
		// grid.setSortEnabled(true);
	}

	/**
	 * Returns the currency symbol to be used in a certain table. This will return
	 * the custom currency symbol for a certain table, or the default currency
	 * symbol if no custmo currency symbol is set
	 * 
	 * @param table
	 *            the table
	 * @return
	 */
	public static <T> String getCurrencySymbol(Grid<T> grid) {
		String cs = null;
		if (grid instanceof ModelBasedTable) {
			cs = ((ModelBasedTable<?, ?>) grid).getCurrencySymbol();
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
