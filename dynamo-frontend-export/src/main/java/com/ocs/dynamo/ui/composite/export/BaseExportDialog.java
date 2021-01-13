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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.dialog.BaseModalDialog;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Base class for export dialogs
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID of the entity to export
 * @param <T>  the type of the entity to export
 */
public abstract class BaseExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

	protected static final String EXTENSION_CSV = ".csv";

	protected static final String EXTENSION_XLS = ".xlsx";

	protected static final Logger LOG = LoggerFactory.getLogger(BaseExportDialog.class);

	private static final long serialVersionUID = 2066899457738401866L;

	private final EntityModel<T> entityModel;

	private DownloadButton exportCsvButton;

	private DownloadButton exportExcelButton;

	private final ExportMode exportMode;

	private final ExportService exportService;

	private ProgressBar progressBar;

	private UI ui;

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
		this.ui = UI.getCurrent();
	}

	protected abstract DownloadButton createDownloadCSVButton();

	protected abstract DownloadButton createDownloadExcelButton();

	@Override
	protected void doBuild(VerticalLayout parent) {

		progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);

		exportExcelButton = createDownloadExcelButton();
		parent.add(exportExcelButton);

		exportCsvButton = createDownloadCSVButton();
		parent.add(exportCsvButton);

		UI.getCurrent().setPollInterval(100);
		parent.add(progressBar);
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		Button cancelButton = new Button(message("ocs.cancel"));
		cancelButton.addClickListener(event -> close());
		cancelButton.setIcon(VaadinIcon.BAN.create());
		buttonBar.add(cancelButton);
	}

	/**
	 * Creates the download stream
	 * 
	 * @param supplier supplier function
	 * @return
	 */
	protected InputStream download(Supplier<ByteArrayInputStream> supplier) {
		try {
			ByteArrayInputStream stream = supplier.get();
			getProgressBar().setVisible(false);
			this.close();
			return stream;
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			getUi().access(() -> VaadinUtils.showErrorNotification(ex.getMessage()));
			return null;
		} finally {
			this.close();
		}
	}

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public DownloadButton getExportCsvButton() {
		return exportCsvButton;
	}

	public DownloadButton getExportExcelButton() {
		return exportExcelButton;
	}

	public ExportMode getExportMode() {
		return exportMode;
	}

	public ExportService getExportService() {
		return exportService;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	protected String getStyleName() {
		return "ocsDownloadDialog";
	}

	@Override
	protected String getTitle() {
		return message("ocs.export");
	}

	public UI getUi() {
		return ui;
	}
}
