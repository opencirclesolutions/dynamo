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
import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.provider.PivotAggregationType;
import com.ocs.dynamo.ui.provider.PivotDataProvider;
import com.ocs.dynamo.ui.provider.PivotedItem;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

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

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * 
	 * @param provider           the pivot data provider
	 * @param possibleColumnKeys the possible column key data
	 * @param fixedHeaderMapper  function used to map from fixed column property to
	 *                           grid header
	 * @param headerMapper       function used to map from variable column property
	 *                           to grid header
	 */
	public PivotGrid(PivotDataProvider<ID, T> provider, List<Object> possibleColumnKeys,
			Function<String, String> fixedHeaderMapper, BiFunction<Object, Object, String> headerMapper,
			BiFunction<String, Object, String> customFormatter) {
		setDataProvider(provider);
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

		for (int i = 0; i < provider.getFixedColumnKeys().size(); i++) {
			String fk = provider.getFixedColumnKeys().get(i);
			addColumn(t -> t.getFixedValue(fk)).setHeader(fixedHeaderMapper.apply(fk)).setFrozen(true)
					.setAutoWidth(true).setKey(fk).setId(fk);
		}

		for (int i = 0; i < possibleColumnKeys.size(); i++) {
			Object pk = possibleColumnKeys.get(i);
			for (String property : provider.getPivotedProperties()) {
				addColumn(t -> {
					// first check for a custom formatter. Otherwise fall back to default formatting
					String value = null;
					if (customFormatter != null) {
						value = customFormatter.apply(property, t.getValue(pk, property));
					}
					if (StringUtils.isEmpty(value)) {
						value = t.getFormattedValue(pk, property);
					}
					return value;
				}).setHeader(headerMapper.apply(pk, property)).setAutoWidth(true).setKey(pk + "_" + property)
						.setId(pk + "_" + property);
			}
		}

		for (String property : provider.getPivotedProperties()) {
			// add aggregate column
			PivotAggregationType type = provider.getAggregation(property);
			if (type != null) {
				String header = getAggregateHeader(type);
				String colId = "aggregate_" + type.toString().toLowerCase();

				addColumn(t -> {
					BigDecimal bd = null;
					if (PivotAggregationType.SUM.equals(type)) {
						bd = t.getSumValue(property);
					} else if (PivotAggregationType.AVERAGE.equals(type)) {
						bd = t.getAverageValue(property);
					} else {
						bd = BigDecimal.valueOf(t.getCountValue(property));
					}
					return getFormattedAggregateValue(bd, provider.getAggregationClass(property));

				}).setHeader(header).setKey(colId).setId(colId);
			}
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private String getAggregateHeader(PivotAggregationType type) {
		switch (type) {
		case SUM:
			return messageService.getMessage("ocs.sum", VaadinUtils.getLocale());
		case AVERAGE:
			return messageService.getMessage("ocs.average", VaadinUtils.getLocale());
		case COUNT:
			return messageService.getMessage("ocs.count", VaadinUtils.getLocale());
		}
		return null;
	}

	public String getFormattedAggregateValue(BigDecimal bd, Class<?> clazz) {
		if (bd == null) {
			return "";
		}

		if (BigDecimal.class.equals(clazz)) {
			return VaadinUtils.bigDecimalToString(false, true, bd);
		} else if (Long.class.equals(clazz)) {
			return VaadinUtils.longToString(true, false, bd.longValue());
		} else if (Integer.class.equals(clazz)) {
			return VaadinUtils.integerToString(true, false, bd.intValue());
		}
		return bd.toString();
	}

}
