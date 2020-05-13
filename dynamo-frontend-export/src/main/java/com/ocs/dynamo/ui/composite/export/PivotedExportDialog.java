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
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A simple dialog window that offers several buttons for exporting pivoted data
 * to various formats
 * 
 * @author Bas Rutten
 *
 * @param <ID> the class of the ID of the entity that is being exported
 * @param <T>  the class of the entity that is being exported
 */
public class PivotedExportDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseExportDialog<ID, T> {

    private static final long serialVersionUID = -7559490010581729532L;

    private final SerializablePredicate<T> predicate;

    private final List<SortOrder<?>> sortOrders;

    private final FetchJoinInformation[] joins;

    private CustomXlsStyleGenerator<ID, T> customGenerator;

    private PivotParameters pivotParameters;

    /**
     * Constructor
     * 
     * @param exportService   the export service
     * @param entityModel     the entity model of the entity to export
     * @param exportMode      the export mode
     * @param predicate       predicated used to filter the results
     * @param sortOrders      sort orders to apply to the results
     * @param customGenerator custom style generator
     * @param joins
     */
    public PivotedExportDialog(ExportService exportService, EntityModel<T> entityModel, SerializablePredicate<T> predicate,
            List<SortOrder<?>> sortOrders, CustomXlsStyleGenerator<ID, T> customGenerator, PivotParameters pivotParameters,
            FetchJoinInformation... joins) {
        super(exportService, entityModel, ExportMode.FULL);
        this.predicate = predicate;
        this.sortOrders = sortOrders;
        this.customGenerator = customGenerator;
        this.joins = joins;
        this.pivotParameters = pivotParameters;
    }

    @Override
    protected DownloadButton createDownloadCSVButton() {
        return new DownloadButton(message("ocs.export.csv"),
                () -> new ByteArrayInputStream(
                        getExportService().exportCsvPivot(getEntityModel(), predicate, sortOrders, pivotParameters, joins)),
                () -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now() + EXTENSION_CSV);
    }

    @Override
    protected DownloadButton createDownloadExcelButton() {
        return new DownloadButton(message("ocs.export.excel"),
                () -> new ByteArrayInputStream(getExportService().exportExcelPivot(getEntityModel(), predicate, sortOrders, customGenerator,
                        pivotParameters, joins)),
                () -> getEntityModel().getDisplayNamePlural(VaadinUtils.getLocale()) + "_" + LocalDateTime.now() + EXTENSION_XLS);
    }

}
