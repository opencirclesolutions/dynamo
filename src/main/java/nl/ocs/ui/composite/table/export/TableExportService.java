package nl.ocs.ui.composite.table.export;

import com.vaadin.addon.tableexport.ExcelExport;

/**
 * A table that we wrap around a table export so that we can easily intercept the call
 * @author bas.rutten
 *
 */
public class TableExportService {

	/**
	 * Performs an export from a table
	 * @param export
	 */
	public void export(ExcelExport export) {
		export.export();
	}
}
