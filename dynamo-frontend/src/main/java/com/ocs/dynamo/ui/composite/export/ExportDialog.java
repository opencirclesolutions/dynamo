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
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.dialog.BaseModalDialog;
import com.ocs.dynamo.ui.composite.layout.ExportMode;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * A simple dialog window that offers several buttons for exporting data to
 * various formats
 * 
 * @author Bas Rutten
 *
 * @param <ID> the class of the ID of the entity that is being exported
 * @param <T> the class of the entity that is being exported
 */
public class ExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseModalDialog {

	private static final long serialVersionUID = -7559490010581729532L;

	private ExportService exportService = ServiceLocatorFactory.getServiceLocator().getService(ExportService.class);

	private EntityModel<T> entityModel;

	private SerializablePredicate<T> predicate;

	private List<SortOrder<?>> sortOrders;

	private FetchJoinInformation[] joins;

	private ExportMode exportMode;

	/**
	 * Constructor
	 * 
	 * @param entityModel
	 * @param predicate
	 * @param sortOrders
	 * @param joins
	 */
	public ExportDialog(EntityModel<T> entityModel, ExportMode exportMode, SerializablePredicate<T> predicate,
			List<SortOrder<?>> sortOrders, FetchJoinInformation... joins) {
		this.entityModel = entityModel;
		this.exportMode = exportMode;
		this.predicate = predicate;
		this.sortOrders = sortOrders;
		this.joins = joins;
	}

	@Override
	protected void doBuild(Layout parent) {
		DownloadButton exportExcelButton = new DownloadButton(message("ocs.export.excel"), () -> {
			return new ByteArrayInputStream(
					exportService.exportExcel(entityModel, exportMode, predicate, sortOrders, joins));
		}, () -> entityModel.getDisplayNamePlural() + "_" + LocalDateTime.now() + ".xlsx", false);
		parent.addComponent(exportExcelButton);

		DownloadButton exportCsvButton = new DownloadButton(message("ocs.export.csv"), () -> {
			return new ByteArrayInputStream(
					exportService.exportCsv(entityModel, exportMode, predicate, sortOrders, joins));
		}, () -> entityModel.getDisplayNamePlural() + "_" + LocalDateTime.now() + ".csv", false);
		parent.addComponent(exportCsvButton);
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		Button cancelButton = new Button(message("ocs.cancel"));
		cancelButton.addClickListener(event -> close());
		buttonBar.addComponent(cancelButton);
	}

	@Override
	protected String getTitle() {
		return message("ocs.export");
	}

}
