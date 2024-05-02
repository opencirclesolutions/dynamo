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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.TriFunction;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.QuadConsumer;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

import lombok.Getter;
import lombok.Setter;

/**
 * A custom tree grid for displaying a hierarchical data collection. This grid
 * allows data modification although in a rather cumbersome way. It is only
 * suitable for displaying fairly limited data collections since it loads
 * everything in memory
 * 
 * @author bas.rutten
 * @param <ID>  type of the primary key of the child entity
 * @param <U>   type of the child entity
 * @param <ID2> type of the primary key of the parent entity
 * @param <V>   type of the parent entity
 */
@SuppressWarnings({ "serial", "unchecked" })
public class InMemoryTreeGrid<T, ID, C extends AbstractEntity<ID>, ID2, P extends AbstractEntity<ID2>>
		extends TreeGrid<T> implements Buildable {

	/**
	 * The code that is carried out to collect the data that is used to create the
	 * child rows for a specific parent
	 */
	@Getter
	@Setter
	private Function<P, List<C>> childCollector;

	/**
	 * The code that is carried out to create a child row
	 */
	@Getter
	@Setter
	private BiFunction<C, P, T> childRowCreator;

	/**
	 * The code that is carried out to create the columns of the grid
	 */
	@Getter
	@Setter
	private Runnable columnCreator = () -> {
	};

	@Getter
	@Setter
	private Function<T, String> customStyleCreator = row -> null;

	@Getter
	@Setter
	private Function<String, Class<?>> editablePropertyClassCollector = prop -> Integer.class;

	/**
	 * Callback method that is executed to check whether editing is allowed
	 */
	@Getter
	@Setter
	private BooleanSupplier editAllowed = () -> true;

	@Getter
	@Setter
	private String gridHeight = SystemPropertyUtils.getDefaultGridHeight();

	@Getter
	@Setter
	private String lastClickedColumnKey;

	@Getter
	@Setter
	private T lastClickedRow;

	@Getter
	private MessageService messageService;

	/**
	 * The code that is carried out to collect the data that is used to create the
	 * parent rows
	 */
	@Getter
	@Setter
	private Supplier<List<P>> parentCollector;

	/**
	 * The code that is carried out to create a parent row
	 */
	@Getter
	@Setter
	private Function<P, T> parentRowCreator;

	@Getter
	@Setter
	private TriFunction<T, Integer, String, Number> sumCellExtractor;

	/**
	 * 
	 */
	@Getter
	@Setter
	private QuadConsumer<T, Integer, String, BigDecimal> sumCellValueCreator;

	@Getter
	@Setter
	private String[] sumColumns;

	public InMemoryTreeGrid() {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
	}

	/**
	 * Adds a read only column
	 * 
	 * @param propertyName the name of the property
	 * @param caption      the column caption
	 * @param alignRight   whether to align the column to the right
	 */
	public Column<?> addReadOnlyColumn(String propertyName, String caption, boolean alignRight) {
		Column<?> col = null;
		if (this.getColumns().isEmpty()) {
			col = this.addHierarchyColumn(t -> {
				Object value = ClassUtils.getFieldValue(t, propertyName);
				if (value instanceof Number) {
					return convertToString((Number) value, propertyName);
				}
				return value == null ? null : value.toString();
			});
		} else {
			col = this.addColumn(t -> {
				Object value = ClassUtils.getFieldValue(t, propertyName);
				if (value instanceof Number) {
					return convertToString((Number) value, propertyName);
				}
				return value == null ? null : value.toString();
			});
		}

		col.setId(propertyName);
		col.setHeader(caption);
		col.setKey(propertyName);
		col.getElement().setAttribute("title", caption);
		if (alignRight) {
			col.setClassNameGenerator(c -> "alignRight");
		}
		return col;
	}

	@Override
	public void build() {
		setWidthFull();
		setHeight(gridHeight);

		TreeDataProvider<T> provider = (TreeDataProvider<T>) getDataProvider();
		TreeData<T> data = provider.getTreeData();

		// retrieve the parent rows to display
		final List<P> parentCollection = parentCollector.get();
		for (P parent : parentCollection) {

			T parentRow = parentRowCreator.apply(parent);
			data.addItem(null, parentRow);

			List<C> children = childCollector.apply(parent);
			for (C child : children) {
				T childRow = childRowCreator.apply(child, parent);
				data.addItem(parentRow, childRow);
			}
			expand(parentRow);
		}

		setClassNameGenerator(t -> {
			if (data.getRootItems().contains(t)) {
				return DynamoConstants.CSS_PARENT_ROW;
			}
			return customStyleCreator.apply(t);
		});

		if (columnCreator != null) {
			columnCreator.run();
		}

		updateSums();

		this.addItemClickListener(event -> {
			this.lastClickedColumnKey = event.getColumn().getKey();
			this.lastClickedRow = event.getItem();
		});

		setEnabled(checkEditAllowed());
	}

	public boolean checkEditAllowed() {
		return editAllowed == null ? true : editAllowed.getAsBoolean();
	}

	/**
	 * Converts a numeric value from its BigDecimal representation to its native
	 * form
	 * 
	 * @param value      the value
	 * @param propertyId the ID of the property
	 * @return
	 */
	protected Number convertNumber(BigDecimal value, String propertyId) {
		Class<?> clazz = editablePropertyClassCollector.apply(propertyId);
		if (NumberUtils.isInteger(clazz)) {
			return value.intValue();
		} else if (NumberUtils.isLong(clazz)) {
			return value.longValue();
		} else if (clazz.equals(BigDecimal.class)) {
			return value;
		}
		return null;
	}

	/**
	 * Converts a numeric value to its String representation
	 * 
	 * @param value        the BigDecimal value
	 * @param propertyName the name of the property
	 * @return
	 */
	private String convertToString(Number value, String propertyName) {
		if (value == null) {
			return null;
		}

		Class<?> clazz = editablePropertyClassCollector.apply(propertyName);
		if (clazz.equals(Integer.class)) {
			return VaadinUtils.integerToString(true, false, value.intValue());
		} else if (clazz.equals(Long.class)) {
			return VaadinUtils.longToString(true, false, value.longValue());
		} else if (clazz.equals(BigDecimal.class)) {
			return VaadinUtils.bigDecimalToString(false, true, BigDecimal.valueOf(value.doubleValue()));
		}
		return null;
	}

	/**
	 * Extracts the sum cell value
	 * 
	 * @param t
	 * @param columnName
	 * @return
	 */
	private Number extractSumCellValue(T t, int index, String columnName) {
		if (sumCellExtractor == null) {
			return null;
		}
		return sumCellExtractor.apply(t, index, columnName);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	/**
	 * Converts a numeric value to a BigDecimal
	 * 
	 * @param value the value to convert
	 * @return
	 */
	protected BigDecimal toBigDecimal(Number value) {
		return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value.doubleValue());
	}

	/**
	 * Updates a single sum value
	 * 
	 * @param column the column for which to update the sum
	 */
	public void updateSum(String column) {

		TreeDataProvider<T> provider = (TreeDataProvider<T>) getDataProvider();
		TreeData<T> data = provider.getTreeData();
		BigDecimal sum = null;

		// update the sum columns on the parent level
		int index = 0;
		for (T pRow : data.getRootItems()) {
			List<T> cRows = data.getChildren(pRow);
			int j = index;
			sum = cRows.stream().map(c -> extractSumCellValue(c, j, column)).map(n -> toBigDecimal(n))
					.reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
			sumCellValueCreator.accept(pRow, index, column, sum);
			provider.refreshItem(pRow);
		}
		index++;

		FooterRow footerRow = null;
		if (getFooterRows().isEmpty()) {
			footerRow = appendFooterRow();
		} else {
			footerRow = getFooterRows().get(0);
		}

		Column<?> columnByKey = getColumnByKey(column);
		if (columnByKey != null) {
			footerRow.getCell(columnByKey).setText(convertToString(sum, column));
		}

	}

	public void updateSums() {

		TreeDataProvider<T> provider = (TreeDataProvider<T>) getDataProvider();
		TreeData<T> data = provider.getTreeData();

		String[] sumCols = getSumColumns();
		// update the sum columns on the parent level
		// footer sums
		Map<String, BigDecimal> totalSumMap = new HashMap<>();
		if (sumCols == null) {
			sumCols = new String[0];
		}

		// update the sum columns on the parent level
		int index = 0;
		for (String column : sumCols) {
			for (T pRow : data.getRootItems()) {
				List<T> cRows = data.getChildren(pRow);
				int j = index;

				BigDecimal sum = cRows.stream().map(c -> extractSumCellValue(c, j, column)).map(n -> toBigDecimal(n))
						.reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
				BigDecimal ts = totalSumMap.getOrDefault(column, BigDecimal.ZERO);
				totalSumMap.put(column, ts.add(sum));
				sumCellValueCreator.accept(pRow, index, column, sum);
				provider.refreshItem(pRow);
			}
			index++;
		}

		// update the footer sums
		if (sumCols.length > 0) {
			FooterRow footerRow = null;
			if (getFooterRows().isEmpty()) {
				footerRow = appendFooterRow();
			} else {
				footerRow = getFooterRows().get(0);
			}
			for (String column : sumCols) {
				BigDecimal bd = totalSumMap.get(column);
				Column<?> columnByKey = getColumnByKey(column);
				if (columnByKey != null) {
					footerRow.getCell(columnByKey).setText(convertToString(bd, column));
				}
			}
		}
	}
}
