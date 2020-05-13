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
import java.util.function.Supplier;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;

/**
 * A simple dialog window that offers several buttons for exporting data to
 * various formats
 * 
 * @author Bas Rutten
 *
 * @param <ID> the class of the ID of the entity that is being exported
 * @param <T>  the class of the entity that is being exported
 */
public class FixedExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseExportDialog<ID, T> {

	private static final long serialVersionUID = -7559490010581729532L;

	/**
	 * The supplier method for retrieving the list of items
	 */
	private Supplier<List<T>> itemsSupplier;

	/**
	 * The custom style generator
	 */
	private CustomXlsStyleGenerator<ID, T> customGenerator;

	/**
	 * Constructor
	 * 
	 * @param exportService
	 * @param entityModel
	 * @param exportMode
	 * @param customGenerator
	 * @param itemsSupplier
	 */
	public FixedExportDialog(ExportService exportService, EntityModel<T> entityModel, ExportMode exportMode,
			CustomXlsStyleGenerator<ID, T> customGenerator, Supplier<List<T>> itemsSupplier) {
		super(exportService, entityModel, exportMode);
		this.itemsSupplier = itemsSupplier;
		this.customGenerator = customGenerator;
	}

	@Override
	protected DownloadButton createDownloadCSVButton() {
		return new DownloadButton(message("ocs.export.csv"),
				() -> new ByteArrayInputStream(
						getExportService().exportCsvFixed(getEntityModel(), getExportMode(), itemsSupplier.get())),
				() -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now()
						+ EXTENSION_CSV);
	}

	@Override
	protected DownloadButton createDownloadExcelButton() {
		return new DownloadButton(message("ocs.export.excel"),
				() -> new ByteArrayInputStream(getExportService().exportExcelFixed(getEntityModel(), getExportMode(),
						customGenerator, itemsSupplier.get())),
				() -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now()
						+ EXTENSION_XLS);
	}

}
