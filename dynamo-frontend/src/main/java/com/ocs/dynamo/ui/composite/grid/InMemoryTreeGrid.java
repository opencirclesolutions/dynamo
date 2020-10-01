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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

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
public abstract class InMemoryTreeGrid<T, ID, C extends AbstractEntity<ID>, ID2, P extends AbstractEntity<ID2>>
		extends TreeGrid<T> implements Buildable {

	private String gridHeight = SystemPropertyUtils.getDefaultGridHeight();

	private MessageService messageService;

	private String lastClickedColumnKey;

	private T lastClickedRow;

	public InMemoryTreeGrid() {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
	}

	/**
	 * Adds the necessary columns
	 */
	protected abstract void addColumns();

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
		final List<P> parentCollection = getParentCollection();
		for (P parent : parentCollection) {

			T t = createParentRow(parent);
			data.addItem(null, t);

			List<C> children = getChildren(parent);
			for (C child : children) {

				T t2 = createChildRow(child, parent);
				data.addItem(t, t2);
			}
			expand(t);
		}

		setClassNameGenerator(t -> {
			if (data.getRootItems().contains(t)) {
				// separate style for parent row
				return DynamoConstants.CSS_PARENT_ROW;
			}
			return getCustomStyle(t);
		});

		addColumns();
		updateSums();

		this.addItemClickListener(event -> {
			this.lastClickedColumnKey = event.getColumn().getKey();
			this.lastClickedRow = event.getItem();
		});

		setEnabled(isEditAllowed());

	}

	public void updateSums() {

		TreeDataProvider<T> provider = (TreeDataProvider<T>) getDataProvider();
		TreeData<T> data = provider.getTreeData();

		String[] sumColumns = getSumColumns();
		// update the sum columns on the parent level
		// footer sums
		Map<String, BigDecimal> totalSumMap = new HashMap<>();
		if (sumColumns == null) {
			sumColumns = new String[0];
		}
		for (String s : sumColumns) {
			totalSumMap.put(s, BigDecimal.ZERO);
		}

		// update the sum columns on the parent level
		int index = 0;
		for (String column : sumColumns) {
			for (T pRow : data.getRootItems()) {
				List<T> cRows = data.getChildren(pRow);
				int j = index;
				BigDecimal sum = cRows.stream().map(c -> extractSumCellValue(c, j, column)).map(n -> toBigDecimal(n))
						.reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
				BigDecimal ts = totalSumMap.get(column);
				totalSumMap.put(column, ts.add(sum));
				setSumCellValue(pRow, index, column, sum);
				provider.refreshItem(pRow);
			}
			index++;
		}

		// update the footer sums
		if (sumColumns.length > 0) {
			FooterRow footerRow = null;
			if (getFooterRows().isEmpty()) {
				footerRow = appendFooterRow();
			} else {
				footerRow = getFooterRows().get(0);
			}
			for (String column : sumColumns) {
				BigDecimal bd = totalSumMap.get(column);
				Column<?> columnByKey = getColumnByKey(column);
				if (columnByKey != null) {
					footerRow.getCell(columnByKey).setText(convertToString(bd, column));
				}
			}
		}
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
		Class<?> clazz = getEditablePropertyClass(propertyId);
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

		Class<?> clazz = getEditablePropertyClass(propertyName);
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
	 * Creates a child row
	 * 
	 * @param childEntity  the child entity used to fill the row
	 * @param parentEntity the parent entity of the child entity
	 * @return
	 */
	protected abstract T createChildRow(C childEntity, P parentEntity);

	/**
	 * Creates a parent row
	 * 
	 * @param entity the entity that is used to fill the parent row
	 * @return
	 */
	protected abstract T createParentRow(P entity);

	/**
	 * Extracts the sum cell value
	 * 
	 * @param t
	 * @param columnName
	 * @return
	 */
	protected abstract Number extractSumCellValue(T t, int index, String columnName);

	/**
	 * Returns the children of the provided parent entity
	 * 
	 * @return
	 */
	protected abstract List<C> getChildren(P parent);

	/**
	 * Returns the custom style to use for a certain row
	 * 
	 * @param t the item that is displayed in the row
	 * @return
	 */
	protected String getCustomStyle(T t) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Returns the class for an editable property
	 * 
	 * @param propertyName the name of the property
	 * @return
	 */
	protected abstract Class<?> getEditablePropertyClass(String propertyName);

	public String getGridHeight() {
		return gridHeight;
	}

	/**
	 * 
	 * @return
	 */
	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Returns the entities used for filling the parent rows
	 * 
	 * @return
	 */
	protected abstract List<P> getParentCollection();

	/**
	 * Returns the property IDs of the columns for which a sum (on the parent level)
	 * must be calculated
	 * 
	 * @return
	 */
	protected abstract String[] getSumColumns();

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	public void setGridHeight(String gridHeight) {
		this.gridHeight = gridHeight;
	}

	/**
	 * Sets the sum cell value
	 * 
	 * @param t
	 * @param index
	 * @param columnName
	 * @param value
	 */
	protected abstract void setSumCellValue(T t, int index, String columnName, BigDecimal value);

	/**
	 * Converts a numeric value to a BigDecimal
	 * 
	 * @param value the value to convert
	 * @return
	 */
	protected BigDecimal toBigDecimal(Number value) {
		return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value.doubleValue());
	}

	public String getLastClickedColumnKey() {
		return lastClickedColumnKey;
	}

	public void setLastClickedColumnKey(String lastClickedColumnKey) {
		this.lastClickedColumnKey = lastClickedColumnKey;
	}

	public T getLastClickedRow() {
		return lastClickedRow;
	}

	public void setLastClickedRow(T lastClickedRow) {
		this.lastClickedRow = lastClickedRow;
	}

	protected abstract boolean isEditAllowed();
}
