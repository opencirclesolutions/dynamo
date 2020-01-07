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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.ui.provider.PivotDataProvider;
import com.ocs.dynamo.ui.provider.PivotedItem;
import com.vaadin.flow.component.grid.Grid;

/**
 * 
 * @author Bas Rutten
 *
 *         A grid that is used to display a collection of pivoted data. Pivoted
 *         data is data that is aggregated so that multiple rows from the flat
 *         data set are transformed into a single row.
 * 
 *         This pivot grid assumes that there are a number of possible values
 *         for a certain field (the field to pivot on) and each possible value
 *         leads to a column in the pivot table.
 *
 * @param <ID> the type of the private key
 * @param <T>  the type of the entity
 */
public class PivotGrid<ID extends Serializable, T extends AbstractEntity<ID>> extends Grid<PivotedItem> {

    private static final long serialVersionUID = -1302975905471267532L;

    /**
     * 
     * @param provider           the pivot data provider
     * @param possibleColumnKeys the possible column key data
     * @param fixedHeaderMapper  function used to map from fixed column property to
     *                           grid header
     * @param headerMapper       function used to map from variable column property
     *                           to grid header
     */
    public PivotGrid(PivotDataProvider<ID, T> provider, List<Object> possibleColumnKeys, Function<String, String> fixedHeaderMapper,
            BiFunction<Object, Object, String> headerMapper) {

        setDataProvider(provider);

        for (int i = 0; i < provider.getFixedColumnKeys().size(); i++) {
            String fk = provider.getFixedColumnKeys().get(i);
            addColumn(t -> t.getFixedValue(fk)).setHeader(fixedHeaderMapper.apply(fk)).setFrozen(true).setAutoWidth(true).setKey(fk)
                    .setId(fk);
        }

        for (int i = 0; i < possibleColumnKeys.size(); i++) {
            Object pk = possibleColumnKeys.get(i);
            for (String property : provider.getPivotedProperties()) {
                addColumn(t -> t.getValue(pk, property)).setHeader(headerMapper.apply(pk, property)).setAutoWidth(true)
                        .setKey(pk + "_" + property).setId(pk + "_" + property);
            }
        }
    }

}
