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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A simple dialog window that offers several buttons for exporting paginated data to
 * various formats
 *
 * @param <ID> the class of the ID of the entity that is being exported
 * @param <T>  the class of the entity that is being exported
 * @author Bas Rutten
 */
public class ExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseExportDialog<ID, T> {

    private static final long serialVersionUID = -7559490010581729532L;

    private final SerializablePredicate<T> predicate;

    private final List<SortOrder<?>> sortOrders;

    private final FetchJoinInformation[] joins;

    private final Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator;

    /**
     * Constructor
     *
     * @param exportService   the export service
     * @param entityModel     the entity model of the entity to export
     * @param exportMode      the desired export mode
     * @param predicate       predicate used to filter the results
     * @param sortOrders      the sort orders
     * @param customGenerator custom style generator
     * @param joins           the joins to use when fetching data
     */
    public ExportDialog(ExportService exportService, EntityModel<T> entityModel, ExportMode exportMode,
                        SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders,
                        Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, FetchJoinInformation... joins) {
        super(exportService, entityModel, exportMode);
        this.predicate = predicate;
        this.sortOrders = sortOrders;
        this.customGenerator = customGenerator;
        this.joins = joins;
    }

    @Override
    protected DownloadButton createDownloadCSVButton() {
        return new DownloadButton(message("ocs.export.csv"), getProgressBar(),
                () -> download(() -> new ByteArrayInputStream(
                        getExportService().exportCsv(getEntityModel(), getExportMode(), predicate, sortOrders, joins))),
                () -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now()
                        + EXTENSION_CSV);
    }

    @Override
    protected DownloadButton createDownloadExcelButton() {
        return new DownloadButton(message("ocs.export.excel"), getProgressBar(),
                () -> download(() -> new ByteArrayInputStream(getExportService().exportExcel(getEntityModel(),
                        getExportMode(), predicate, sortOrders, customGenerator, joins))),
                () -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now()
                        + EXTENSION_XLS);
    }

}
