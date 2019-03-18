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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.dialog.BaseModalDialog;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * Base class for export dialogs
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public abstract class BaseExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

	private static final long serialVersionUID = 2066899457738401866L;

	protected static final String EXTENSION_CSV = ".csv";

	protected static final String EXTENSION_XLS = ".xlsx";

	private final ExportService exportService;

	private final ExportMode exportMode;

	private final EntityModel<T> entityModel;

	/**
	 * Constructor
	 * 
	 * @param exportService the export button
	 * @param entityModel   the entity model of the entity to export
	 * @param exportMode    the export mode
	 */
	public BaseExportDialog(ExportService exportService, EntityModel<T> entityModel, ExportMode exportMode) {
		this.entityModel = entityModel;
		this.exportService = exportService;
		this.exportMode = exportMode;
	}

	protected abstract Button createDownloadCSVButton();

	protected abstract Button createDownloadExcelButton();

	@Override
	protected void doBuild(Layout parent) {
		Button exportExcelButton = createDownloadExcelButton();
		parent.addComponent(exportExcelButton);
		Button exportCsvButton = createDownloadCSVButton();
		parent.addComponent(exportCsvButton);
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		Button cancelButton = new Button(message("ocs.cancel"));
		cancelButton.addClickListener(event -> close());
		cancelButton.setIcon(VaadinIcons.BAN);
		buttonBar.addComponent(cancelButton);
	}

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public ExportMode getExportMode() {
		return exportMode;
	}

	public ExportService getExportService() {
		return exportService;
	}

	@Override
	protected String getTitle() {
		return message("ocs.export");
	}

}
