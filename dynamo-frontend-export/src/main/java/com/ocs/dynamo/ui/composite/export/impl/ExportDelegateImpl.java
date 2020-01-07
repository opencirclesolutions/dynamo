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
package com.ocs.dynamo.ui.composite.export.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.ExportDelegate;
import com.ocs.dynamo.ui.composite.export.ExportDialog;
import com.ocs.dynamo.ui.composite.export.ExportService;
import com.ocs.dynamo.ui.composite.export.FixedExportDialog;
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.export.PivotedExportDialog;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Implementation of the export delegate
 * 
 * @author Bas Rutten
 *
 */
public class ExportDelegateImpl implements ExportDelegate {

    /**
     * Map for keeping track of custom style generators
     */
    private Map<EntityModel<?>, CustomXlsStyleGenerator<?, ?>> customStyleMap = new HashMap<>();

    @Autowired
    private ExportService exportService;

    /**
     * Adds a mapping between an entity model and a custom style generator
     * 
     * @param entityModel the entity model of the entity
     * @param generator   the custom style generator
     */
    public void addCustomStyleGenerator(EntityModel<?> entityModel, CustomXlsStyleGenerator<?, ?> generator) {
        customStyleMap.put(entityModel, generator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends Serializable, T extends AbstractEntity<ID>> void export(EntityModel<T> entityModel, ExportMode mode,
            SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders, FetchJoinInformation... joins) {
        ExportDialog<ID, T> dialog = new ExportDialog<>(exportService, entityModel, mode, predicate, sortOrders,
                (CustomXlsStyleGenerator<ID, T>) customStyleMap.get(entityModel), joins);
        dialog.buildAndOpen();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends Serializable, T extends AbstractEntity<ID>> void exportFixed(EntityModel<T> entityModel, ExportMode mode,
            Collection<T> items) {
        FixedExportDialog<ID, T> dialog = new FixedExportDialog<>(exportService, entityModel, mode,
                (CustomXlsStyleGenerator<ID, T>) customStyleMap.get(entityModel), () -> Lists.newArrayList(items));
        dialog.buildAndOpen();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ID extends Serializable, T extends AbstractEntity<ID>> void exportPivoted(EntityModel<T> entityModel,
            SerializablePredicate<T> predicate, List<SortOrder<?>> sortOrders, PivotParameters pivotParameters,
            FetchJoinInformation... joins) {
        PivotedExportDialog<ID, T> dialog = new PivotedExportDialog<>(exportService, entityModel, predicate, sortOrders,
                (CustomXlsStyleGenerator<ID, T>) customStyleMap.get(entityModel), pivotParameters, joins);
        dialog.buildAndOpen();
    }

}
