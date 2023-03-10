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
import com.vaadin.flow.component.grid.HeaderRow;

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
	 * Constructor
	 * 
	 * @param provider           the data provider
	 * @param possibleColumnKeys the possible column kyes
	 * @param fixedHeaderMapper  the fixed header mapper
	 * @param headerMapper       the header mapper
	 * @param subHeaderMapper    the sub header mapper
	 * @param customFormatter    custom formatter
	 */
	public PivotGrid(PivotDataProvider<ID, T> provider, List<Object> possibleColumnKeys,
			Function<String, String> fixedHeaderMapper, BiFunction<Object, Object, String> headerMapper,
			BiFunction<Object, Object, String> subHeaderMapper, BiFunction<String, Object, String> customFormatter) {
		setDataProvider(provider);
		// https://vaadin.com/api/platform/23.0.10/deprecated-list.html
		// setItems((Stream<PivotedItem>) provider);
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

		for (int i = 0; i < provider.getFixedColumnKeys().size(); i++) {
			String fk = provider.getFixedColumnKeys().get(i);
			addColumn(t -> t.getFixedValue(fk)).setHeader(fixedHeaderMapper.apply(fk)).setFrozen(true)
					.setResizable(true).setAutoWidth(true).setKey(fk).setId(fk);
		}

		HeaderRow headerRow = null;
		if (getHeaderRows().size() < 2) {
			headerRow = this.appendHeaderRow();
		}

		addColumns(provider, possibleColumnKeys, headerMapper, subHeaderMapper, customFormatter, headerRow);
		List<String> allProperties = provider.getAllPrivotProperties();

		long totalsColumns = allProperties.stream().filter(a -> provider.getAggregation(a) != null).count();

		for (String property : allProperties) {
			// add aggregate column
			PivotAggregationType type = provider.getAggregation(property);

			if (type != null) {
				String header = getAggregateHeader(type);
				String colId = "aggregate_" + type.toString().toLowerCase() + "_" + property;

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

				}).setHeader(header).setKey(colId).setResizable(true).setId(colId);

				if (headerRow != null && totalsColumns > 1) {
					headerRow.getCell(getColumnByKey(colId)).setText(subHeaderMapper.apply("", property));
				}
			}
		}
	}

	private void addColumns(PivotDataProvider<ID, T> provider, List<Object> possibleColumnKeys,
			BiFunction<Object, Object, String> headerMapper, BiFunction<Object, Object, String> subHeaderMapper,
			BiFunction<String, Object, String> customFormatter, HeaderRow headerRow) {
		for (int i = 0; i < possibleColumnKeys.size(); i++) {
			Object columnKey = possibleColumnKeys.get(i);
			for (String property : provider.getPivotedProperties()) {
				addColumn(t -> {
					// first check for a custom formatter. Otherwise fall back to default formatting
					String value = null;
					if (customFormatter != null) {
						value = customFormatter.apply(property, t.getValue(columnKey, property));
					}
					if (StringUtils.isEmpty(value)) {
						value = t.getFormattedValue(columnKey, property);
					}
					return value;
				}).setHeader(headerMapper.apply(columnKey, property)).setAutoWidth(true)
						.setKey(calculateColumnKey(columnKey, property)).setResizable(true)
						.setId(calculateColumnKey(columnKey, property));

				addSubHeaderRow(subHeaderMapper, headerRow, columnKey, property);
			}
		}
	}

	/**
	 * Calculate the key/ID of the grid column
	 * 
	 * @param columnKey the column key
	 * @param property  the property
	 * @return the calculate column key
	 */
	private String calculateColumnKey(Object columnKey, String property) {
		return columnKey + "_" + property;
	}

	/**
	 * Adds a sub header row
	 * 
	 * @param subHeaderMapper the sub header mapper
	 * @param headerRow       the row to which to add the columns
	 * @param columnKey       the column key
	 * @param property        the pivoted property
	 */
	private void addSubHeaderRow(BiFunction<Object, Object, String> subHeaderMapper, HeaderRow headerRow,
			Object columnKey, String property) {
		headerRow.getCell(getColumnByKey(columnKey.toString() + "_" + property))
				.setText(subHeaderMapper.apply(columnKey, property));
	}

	/**
	 * Returns the header to use for a column containing an aggregation
	 * 
	 * @param type the type of the aggregation
	 * @return the header
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
