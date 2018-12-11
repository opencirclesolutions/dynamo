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

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.Action;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.components.grid.FooterRow;

/**
 * A custom tree grid for displaying a hierarchical data collection. This table
 * allows data modification although in a rather cumbersome way. It is only
 * suitable for displaying fairly limited data collections since it loads
 * everything in memory
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key of the child entity
 * @param <U> type of the child entity
 * @param <ID2> type of the primary key of the parent entity
 * @param <V> type of the parent entity
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class InMemoryTreeGrid<T, ID, C extends AbstractEntity<ID>, ID2, P extends AbstractEntity<ID2>>
		extends TreeGrid<T> implements Buildable {

	// the message service
	private MessageService messageService;

	/**
	 * Constructor
	 * 
	 * @param exportAllowed
	 */
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
	public void addReadOnlyColumn(String propertyName, String caption, boolean alignRight) {
		Column<?, ?> col = this.addColumn(t -> ClassUtils.getFieldValueAsString(t, propertyName)).setCaption(caption)
				.setId(propertyName);
		if (alignRight) {
			col.setStyleGenerator(item -> "v-align-right");
		}
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	@Override
	public void build() {
		setSizeFull();

		TreeDataProvider<T> provider = (TreeDataProvider<T>) getDataProvider();
		TreeData<T> data = provider.getTreeData();

		setStyleGenerator(t -> {
			if (data.getRootItems().contains(t)) {
				// separate style for parent row
				return DynamoConstants.CSS_PARENT_ROW;
			}
			return getCustomStyle(t);
		});

		addColumns();
		String[] sumColumns = getSumColumns();

		// footer sums
		Map<String, BigDecimal> totalSumMap = new HashMap<>();
		if (sumColumns == null) {
			sumColumns = new String[0];
		}
		for (String s : sumColumns) {
			totalSumMap.put(s, BigDecimal.ZERO);
		}

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
			}
			index++;
		}

		provider.refreshAll();

		// update the footer sums
		FooterRow footerRow = appendFooterRow();
		for (String column : sumColumns) {
			BigDecimal bd = totalSumMap.get(column);
			footerRow.getCell(column).setText(convertToString(bd, column));
		}

		if (sumColumns.length > 0) {
			setFooterVisible(true);
		}
	}

	/**
	 * 
	 * @param t
	 * @param columnName
	 * @return
	 */
	protected abstract Number extractSumCellValue(T t, int index, String columnName);

	/**
	 * 
	 * @param t
	 * @param index
	 * @param columnName
	 * @param value
	 */
	protected abstract void setSumCellValue(T t, int index, String columnName, BigDecimal value);

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
	private String convertToString(BigDecimal value, String propertyName) {
		if (value == null) {
			return null;
		}

		Class<?> clazz = getEditablePropertyClass(propertyName);
		if (clazz.equals(Integer.class)) {
			return VaadinUtils.integerToString(true, false, value.intValue());
		} else if (clazz.equals(Long.class)) {
			return VaadinUtils.longToString(true, false, value.longValue());
		} else if (clazz.equals(BigDecimal.class)) {
			return VaadinUtils.bigDecimalToString(false, true, value);
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
	 * @return any additional actions to add to the context menu
	 */
	protected List<Action> getAdditionalActions() {
		return Lists.newArrayList();
	}

	/**
	 * Returns the children of the provided parent entity
	 * 
	 * @return
	 */
	protected abstract List<C> getChildren(P parent);

	/**
	 * Returns the custom style to use for a certain row/item
	 * 
	 * @param t the item
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

	/**
	 * Converts a numeric value to a BigDecimal
	 * 
	 * @param value the value to convert
	 * @return
	 */
	protected BigDecimal toBigDecimal(Number value) {
		return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value.doubleValue());
	}

}
