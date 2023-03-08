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
 * A dialog window that offers several buttons for exporting data to
 * various formats. Used for exporting fixed data sets
 *
 * @param <ID> the class of the ID of the entity that is being exported
 * @param <T>  the class of the entity that is being exported
 * @author Bas Rutten
 */
public class FixedExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseExportDialog<ID, T> {

    private static final long serialVersionUID = -7559490010581729532L;

    private final Supplier<List<T>> itemsSupplier;

    private final Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator;

    /**
     * Constructor
     *
     * @param exportService   the export service
     * @param entityModel     the entity model of the entity to export
     * @param exportMode      the desired export mode
     * @param customGenerator custom Excel style generator
     * @param itemsSupplier   the supplier used for retrieving the set of items to export
     */
    public FixedExportDialog(ExportService exportService, EntityModel<T> entityModel, ExportMode exportMode,
                             Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, Supplier<List<T>> itemsSupplier) {
        super(exportService, entityModel, exportMode);
        this.itemsSupplier = itemsSupplier;
        this.customGenerator = customGenerator;
    }

    @Override
    protected DownloadButton createDownloadCSVButton() {
        return new DownloadButton(message("ocs.export.csv"), getProgressBar(),
                () -> download(() -> new ByteArrayInputStream(
                        getExportService().exportCsvFixed(getEntityModel(), getExportMode(), itemsSupplier.get()))),
                () -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now()
                        + EXTENSION_CSV);
    }

    @Override
    protected DownloadButton createDownloadExcelButton() {
        return new DownloadButton(message("ocs.export.excel"), getProgressBar(),
                () -> download(() -> new ByteArrayInputStream(getExportService().exportExcelFixed(getEntityModel(),
                        getExportMode(), customGenerator, itemsSupplier.get()))),
                () -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now()
                        + EXTENSION_XLS);
    }

}
