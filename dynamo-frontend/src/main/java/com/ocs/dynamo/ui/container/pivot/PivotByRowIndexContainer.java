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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * This container pivots data using another container that actually provides the data. Pivoting is done simply by
 * rowindex and nr of given columns.
 * 
 * NOTE: Currently only read-only supported
 * 
 * @author Patrick Deenen
 */
public class PivotByRowIndexContainer implements Container, ItemSetChangeListener, Indexed, Sortable {

	private static final long serialVersionUID = 8057632998281455615L;

	private Container sourceContainer;

	private int nrOfColumns = 2;

	private List<String> propertyIds = null;

	private PivotByRowIndexIdList pivotIdList;
	
	private static final int MAX_CACHE_SIZE = 1000;

	private TreeMap<Integer, PivotByRowIndexItem> cache = new TreeMap<>();

	/**
	 * Constructs a PivotContainer.
	 * 
	 * @param sourceContainer
	 *            Mandatory, must implement container.Indexed. source container supplies the source data to pivot
	 * @param cellPropertyId
	 *            Mandatory defines the property which contains the data for the cell
	 * @param nrOfColumns
	 *            Unsigned number of expected rows
	 */
	public PivotByRowIndexContainer(Container sourceContainer, Object cellPropertyId, int nrOfColumns) {
		if (sourceContainer == null) {
			throw new IllegalArgumentException("sourceContainer is mandatory");
		}
		if (!(sourceContainer instanceof Container.Indexed)) {
			throw new IllegalArgumentException("sourceContainer must implement Container.Indexed");
		}
		if (cellPropertyId == null) {
			throw new IllegalArgumentException("cellPropertyId is mandatory");
		}
		if (nrOfColumns < 2) {
			throw new IllegalArgumentException("nrOfColumns must be >=2");
		}
		this.sourceContainer = sourceContainer;
		this.nrOfColumns = nrOfColumns;
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
		if (propertyIds == null) {
			// Define all properties for each column
			propertyIds = new ArrayList<>();
			for (int i = 0; i < nrOfColumns; i++) {
				propertyIds.add("" + i);
				for (Object id : sourceContainer.getContainerPropertyIds()) {
					propertyIds.add(i + "_" + id);
				}
			}
		}
		return propertyIds;
	}

	/**
	 * @param propertyIds
	 *            the propertyIds to set
	 */
	public void setPropertyIds(List<String> propertyIds) {
		this.propertyIds = propertyIds;
	}

	/**
	 * @param propertyIds
	 *            the propertyIds to set
	 */
	public void setPropertyIds(String... propertyIds) {
		this.propertyIds = Arrays.asList(propertyIds);
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
			pivotIdList = new PivotByRowIndexIdList(this);
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
		if (type == null) {
			type = Object.class;
		}
		return type;
	}

	@Override
	public int size() {
		return new Double(Math.ceil(((double) sourceContainer.size()) / nrOfColumns)).intValue();
	}

	@Override
	public boolean containsId(Object rowId) {
		return asSourceItemId(rowId) != null;
	}

	protected Integer asSourceItemId(Object rowId) {
		if (rowId instanceof Integer) {
			int prow = (Integer) rowId;
			int srow = prow * nrOfColumns;
			if (0 <= srow && srow < sourceContainer.size()) {
				return srow;
			}
		}
		return null;
	}

	@Override
	public Item addItem(Object itemId) {
		// FIXME Add support for adding items
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
		// FIXME Add support for adding properties
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeContainerProperty(Object propertyId) {
		// FIXME Add support for removing properties
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
	 * Search for all source items which combined form a row (PivotItem) in the pivot container.
	 * 
	 * @param rowId
	 * @return The created row
	 */
	private Item createPivotItem(Object rowId) {
		if (cache.containsKey(rowId)) {
			// caching is crucial for performance here
			return cache.get(rowId);
		} else {
			Integer srow = asSourceItemId(rowId);
			PivotByRowIndexItem pi = null;
			if (srow != null) {
				pi = new PivotByRowIndexItem(this, (Indexed) sourceContainer, srow);

				if (cache.size() > MAX_CACHE_SIZE) {
					cache.pollFirstEntry();
				}
				cache.put((Integer) rowId, pi);
			}
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
		cache.clear();
	}

	@Override
	public Object nextItemId(Object rowId) {
		if (rowId instanceof Integer) {
			int nRowId = ((Integer) rowId) + 1;
			Integer nsrow = asSourceItemId(nRowId);
			if (nsrow != null) {
				return getIdByIndex(nRowId);
			}
		}
		return null;
	}

	@Override
	public Object prevItemId(Object rowId) {
		if (rowId instanceof Integer) {
			int nRowId = ((Integer) rowId) - 1;
			Integer nsrow = asSourceItemId(nRowId);
			if (nsrow != null) {
				return getIdByIndex(nRowId);
			}
		}
		return null;
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
			throw new IndexOutOfBoundsException("Start index cannot be negative! startIndex="
					+ startIndex);
		}
		if (startIndex > size()) {
			throw new IndexOutOfBoundsException("Start index exceeds container size! startIndex="
					+ startIndex + " containerLastItemIndex=" + (size() - 1));
		}

		if (numberOfItems < 1) {
			if (numberOfItems == 0) {
				return Collections.emptyList();
			}
			throw new IllegalArgumentException(
					"Cannot get negative amount of items! numberOfItems=" + numberOfItems);
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

	public PivotByRowIndexIdList getPivotIdList() {
		return pivotIdList;
	}
}
