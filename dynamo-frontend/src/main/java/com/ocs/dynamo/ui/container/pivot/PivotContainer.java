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
package com.ocs.dynamo.ui.container.pivot;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This container pivots data using another container that actually provides the
 * data.
 * 
 * @author Patrick Deenen
 */
public class PivotContainer implements Container, ItemSetChangeListener, Indexed, Sortable {

	private static final long serialVersionUID = 8057632998281455615L;

	private Container sourceContainer;

	private Object columnPropertyId;

	private Object rowPropertyId;

	private List<?> columnIds;

	private int rowCount;

	private List<String> propIds;

	private PivotIdList pivotIdList;

	private List<String> pivotedColumnPostFixes;

	private static final int MAX_CACHE_SIZE = 100;

	private Map<Object, PivotItem> cache = new HashMap<>();

	/**
	 * Constructs a PivotContainer.
	 * 
	 * @param sourceContainer
	 *            Mandatory, must implement container.Indexed. source container
	 *            supplies the source data to pivot
	 * @param columnPropertyId
	 *            Mandatory defines the property which contains the column
	 *            designator
	 * @param rowPropertyId
	 *            Mandatory defines the property which contains the row designator
	 * @param columnIds
	 *            Mandatory, size should be at least 1 defines all possible unique
	 *            column values
	 * @param pivotedColumnPostFixes
	 *            The identifiers that should be placed behind each column ID in
	 *            order to generate the columns (this is used to prevent the
	 *            creation of excessive columns)
	 * @param rowCount
	 *            Unsigned number of expected rows
	 */
	public PivotContainer(Container sourceContainer, Object columnPropertyId, Object rowPropertyId, List<?> columnIds,
			List<String> pivotedColumnPostFixes, int rowCount) {
		if (sourceContainer == null) {
			throw new IllegalArgumentException("sourceContainer is mandatory");
		}
		if (!(sourceContainer instanceof Container.Indexed)) {
			throw new IllegalArgumentException("sourceContainer must implement Container.Indexed");
		}
		if (columnPropertyId == null) {
			throw new IllegalArgumentException("columnPropertyId is mandatory");
		}
		if (rowPropertyId == null) {
			throw new IllegalArgumentException("rowPropertyId is mandatory");
		}
		if (columnIds == null) {
			throw new IllegalArgumentException("columnIds is mandatory");
		}
		if (columnIds.isEmpty()) {
			throw new IllegalArgumentException("columnIds size must be > 0");
		}
		if (rowCount < 0) {
			throw new IllegalArgumentException("rowCount must be >=0");
		}
		this.sourceContainer = sourceContainer;
		this.columnPropertyId = columnPropertyId;
		this.rowPropertyId = rowPropertyId;
		this.columnIds = columnIds;
		this.rowCount = rowCount;
		this.pivotedColumnPostFixes = pivotedColumnPostFixes;
		if (sourceContainer instanceof Container.ItemSetChangeNotifier) {
			((Container.ItemSetChangeNotifier) sourceContainer).addItemSetChangeListener(this);
		}
	}

	/**
	 * Returns the (unique) list of the properties managed by the container
	 * 
	 * @return
	 */
	protected List<String> getPropertyIds() {
		if (propIds == null) {
			Set<String> temp = new HashSet<>();
			// First define all properties except columnPropertyId
			for (Object i : sourceContainer.getContainerPropertyIds()) {
				if (!columnPropertyId.equals(i)) {
					temp.add(i.toString());
				}
			}
			// Then define all properties for each column
			for (Object cid : columnIds) {
				for (String postFix : pivotedColumnPostFixes) {
					if (!rowPropertyId.equals(postFix)) {
						temp.add(cid + "_" + postFix);
					}
				}
			}
			propIds = new ArrayList<>(temp);
		}
		return propIds;
	}

	@Override
	public Item getItem(Object itemId) {
		return createPivotItem(itemId);
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return getPropertyIds();
	}

	@Override
	public Collection<?> getItemIds() {
		if (pivotIdList == null) {
			pivotIdList = new PivotIdList(this);
		}
		return pivotIdList;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		return getItem(itemId).getItemProperty(propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		Class<?> type = sourceContainer.getType(propertyId);
		String pid = propertyId.toString();
		int i = pid.lastIndexOf('_');
		if (type == null && i >= 0 && i < pid.length()) {
			type = sourceContainer.getType(pid.substring(i + 1));
		}
		return type;
	}

	@Override
	public int size() {
		return rowCount;
	}

	@Override
	public boolean containsId(Object itemId) {
		return itemId instanceof Integer;
	}

	@Override
	public Item addItem(Object itemId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object addItem() {
		// FIXME Add support for adding items
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeItem(Object itemId) {
		// FIXME Add support for removing items
		return false;
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeContainerProperty(Object propertyId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAllItems() {
		return sourceContainer.removeAllItems();
	}

	/**
	 * @return the source container which provides all data for this container
	 */
	protected Container getSourceContainer() {
		return sourceContainer;
	}

	/**
	 * @return the property id to the value in the item that identifies the column
	 */
	protected Object getColumnPropertyId() {
		return columnPropertyId;
	}

	/**
	 * @return the column ids that identify the column
	 */
	protected List<?> getColumnIds() {
		return columnIds;
	}

	/**
	 * @return the property id to the value in the item that identifies the row
	 */
	protected Object getRowPropertyId() {
		return rowPropertyId;
	}

	/**
	 * Search for all source items which combined form a row (PivotItem) in the
	 * pivot container.
	 * 
	 * @param pivotId
	 * @return The created row
	 */
	private Item createPivotItem(Object pivotId) {
		// TODO support added items to source container or editing in pivot
		// container

		if (cache.containsKey(pivotId)) {
			// caching is crucial for performance here
			return cache.get(pivotId);
		} else {
			Map<Object, Item> columnItems = new HashMap<>();
			int i = (Integer) pivotId;
			Container.Indexed sc = (Container.Indexed) sourceContainer;
			Object rowValue = null;
			// Search items
			Item column = null;
			while (i < sc.size()) {
				column = sc.getItem(sc.getIdByIndex(i));
				if (column != null) {
					Object newRowValue = column.getItemProperty(rowPropertyId).getValue();
					if (rowValue == null) {
						// Initialize the row value
						rowValue = newRowValue;
					}
					if (newRowValue != null && newRowValue.equals(rowValue)) {
						// The rowvalue is the same, hence the item is part of
						// this
						// row
						columnItems.put(column.getItemProperty(columnPropertyId).getValue().toString(), column);
					} else {
						// When the rowvalue is not equal to the first, the item
						// is
						// part of the next row
						break;
					}
				}
				i++;
			}
			PivotItem pi = new PivotItem(columnItems, this);

			if (cache.size() > MAX_CACHE_SIZE) {
				cache.clear();
			}
			cache.put(pivotId, pi);

			return pi;
		}
	}

	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		reset();
	}

	public void reset() {
		// Reset
		pivotIdList = null;
		propIds = null;
		cache.clear();
	}

	@Override
	public Object nextItemId(Object itemId) {
		int index = indexOfId(itemId);
		if (index >= 0 && index < size() - 1) {
			return getIdByIndex(index + 1);
		} else {
			// out of bounds
			return null;
		}
	}

	@Override
	public Object prevItemId(Object itemId) {
		int index = indexOfId(itemId);
		if (index > 0) {
			return getIdByIndex(index - 1);
		} else {
			// out of bounds
			return null;
		}
	}

	@Override
	public Object firstItemId() {
		if (size() > 0) {
			return getIdByIndex(0);
		} else {
			return null;
		}
	}

	@Override
	public Object lastItemId() {
		if (size() > 0) {
			return getIdByIndex(size() - 1);
		} else {
			return null;
		}
	}

	@Override
	public boolean isFirstId(Object itemId) {
		if (itemId == null) {
			return false;
		}
		return itemId.equals(firstItemId());
	}

	@Override
	public boolean isLastId(Object itemId) {
		if (itemId == null) {
			return false;
		}
		return itemId.equals(lastItemId());
	}

	@Override
	public Object addItemAfter(Object previousItemId) {
		// FIXME Add support for removing items
		throw new UnsupportedOperationException();
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId) {
		// FIXME Add support for removing items
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOfId(Object itemId) {
		return ((List<?>) getItemIds()).indexOf(itemId);
	}

	@Override
	public Object getIdByIndex(int index) {
		return ((List<?>) getItemIds()).get(index);
	}

	@Override
	public List<?> getItemIds(int startIndex, int numberOfItems) {
		if (startIndex < 0) {
			throw new IndexOutOfBoundsException("Start index cannot be negative! startIndex=" + startIndex);
		}
		if (startIndex > size()) {
			throw new IndexOutOfBoundsException("Start index exceeds container size! startIndex=" + startIndex
					+ " containerLastItemIndex=" + (size() - 1));
		}

		if (numberOfItems < 1) {
			if (numberOfItems == 0) {
				return Collections.emptyList();
			}
			throw new IllegalArgumentException("Cannot get negative amount of items! numberOfItems=" + numberOfItems);
		}

		int endIndex = startIndex + numberOfItems;
		if (endIndex > size()) {
			endIndex = size();
		}

		return Collections.unmodifiableList(((List<?>) getItemIds()).subList(startIndex, endIndex));
	}

	@Override
	public Object addItemAt(int index) {
		// FIXME Add support for removing items
		throw new UnsupportedOperationException();
	}

	@Override
	public Item addItemAt(int index, Object newItemId) {
		// FIXME Add support for removing items
		throw new UnsupportedOperationException();
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		if (this.rowCount != rowCount) {
			this.rowCount = rowCount;
			reset();
		}
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		// do nothing - we do not actually support sorting
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		if (sourceContainer instanceof Sortable) {
			return ((Sortable) sourceContainer).getSortableContainerPropertyIds();
		}
		return new HashSet<>();
	}

	public PivotIdList getPivotIdList() {
		return pivotIdList;
	}
}
