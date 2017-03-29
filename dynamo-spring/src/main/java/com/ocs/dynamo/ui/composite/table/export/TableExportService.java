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
package com.ocs.dynamo.ui.composite.table.export;

import org.springframework.stereotype.Service;

import com.vaadin.addon.tableexport.TableExport;

/**
 * A table that we wrap around a table export so that we can easily intercept the call
 * 
 * @author bas.rutten
 */
@Service("tableExportService")
public class TableExportService {

	/**
	 * Performs an export from a table
	 * 
	 * @param export
	 *            the TableExport object
	 */
	public void export(TableExport export) {
		export.export();
	}

}
