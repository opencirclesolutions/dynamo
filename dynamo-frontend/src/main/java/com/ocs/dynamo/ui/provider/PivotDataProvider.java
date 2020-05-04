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
package com.ocs.dynamo.ui.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A pivoting data provider that acts as a wrapper around a regular data
 * provider.
 * 
 * This component must be initialized with the lower level entity that is being
 * queried. Multiple rows from the lower level will then be aggregated into a
 * PivotedItem based on the value of the rowKeyProperty.
 * 
 * @author Bas Rutten
 *
 * @param <ID> the primary key of the entity to query
 * @param <T>  the entity to query
 */
public class PivotDataProvider<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractDataProvider<PivotedItem, SerializablePredicate<PivotedItem>> {

	private static final int PAGE_SIZE = 1000;

	private static final long serialVersionUID = 4243018942036820420L;

	/**
	 * Code to carry out after the count query completes
	 */
	private Consumer<Integer> afterCountCompleted;

	/**
	 * The name of the property that contains the identifying value for a column
	 */
	private String columnKeyProperty;

	/**
	 * Cache for keeping track of current page of data
	 */
	private Queue<T> dataCache = new LinkedList<>();

	private List<String> fixedColumnKeys;

	/**
	 * The offset of the latest page that was fetched from the wrapped provider
	 */
	private int lastPivotOffset = 0;

	/**
	 * The offset of the latest page that was requested from the outside
	 */
	private int lastRequestedOffset = 0;

	/**
	 * The "row key" value of the last retrieved row
	 */
	private Object lastRowPropertyValue;

	/**
	 * Mapping from requested offset to offset in the wrapped provider
	 */
	private Map<Integer, Integer> offsetMap = new HashMap<>();

	/**
	 * The pivoted item that is currently being constructed
	 */
	private PivotedItem pivotedItem;

	/**
	 * 
	 */
	private List<String> pivotedProperties;

	/**
	 * The data provider that is being wrapped
	 */
	private BaseDataProvider<ID, T> provider;

	/**
	 * The property that is checked to see if a new row has been reaced
	 */
	private String rowKeyProperty;

	private int size;

	/**
	 * Supplier to carry out to retrieve size of pivoted data set
	 */
	private Supplier<Integer> sizeSupplier;

	/**
	 * 
	 * @param provider          the wrapped data provider
	 * @param rowKeyProperty    the property to check for unique row values
	 * @param columnKeyProperty the property to check for the column key
	 * @param fixedColumnKeys   the fixed columns
	 * @param pivotedProperties the pivoted properties
	 * @param sizeSupplier
	 */
	public PivotDataProvider(BaseDataProvider<ID, T> provider, String rowKeyProperty, String columnKeyProperty,
			List<String> fixedColumnKeys, List<String> pivotedProperties, Supplier<Integer> sizeSupplier) {
		this.provider = provider;
		this.columnKeyProperty = columnKeyProperty;
		this.rowKeyProperty = rowKeyProperty;
		this.fixedColumnKeys = fixedColumnKeys;
		this.pivotedProperties = pivotedProperties;
		this.sizeSupplier = sizeSupplier;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Stream<PivotedItem> fetch(Query<PivotedItem, SerializablePredicate<PivotedItem>> query) {
		// these methods must be called in order to not break the contract
		int requestedOffset = query.getOffset();
		query.getLimit();

		List<PivotedItem> result = new ArrayList<>();
		while (result.size() < query.getLimit()) {

			Optional<SerializablePredicate<T>> sp = (Optional) query.getFilter();

			// re-load earlier page
			if (requestedOffset < lastRequestedOffset && offsetMap.containsKey(requestedOffset)) {
				dataCache.clear();
				lastPivotOffset = offsetMap.get(requestedOffset);
			}

			lastRequestedOffset = requestedOffset;

			// fetch next page
			if (dataCache.isEmpty()) {
				offsetMap.put(requestedOffset, lastPivotOffset);
				Query<T, SerializablePredicate<T>> newQuery = new Query<>(lastPivotOffset, PAGE_SIZE,
						query.getSortOrders(), null, sp.orElse(null));
				provider.fetch(newQuery).forEach(t -> dataCache.add(t));
				lastPivotOffset = lastPivotOffset + PAGE_SIZE;
			}

			if (dataCache.isEmpty()) {
				// no more records left before end of page, abort
				break;
			} else {
				T t = dataCache.poll();

				// get the row value to determine if we need a new row
				Object rowPropertyValue = ClassUtils.getFieldValue(t, rowKeyProperty);

				// create new pivoted item if needed and add existing one to result set
				if (lastRowPropertyValue == null || !Objects.equals(rowPropertyValue, lastRowPropertyValue)) {
					// add previous item
					if (pivotedItem != null) {
						result.add(pivotedItem);
					}
					// create new item
					pivotedItem = new PivotedItem(rowPropertyValue);
					lastRowPropertyValue = rowPropertyValue;
				}

				// add fixed columns
				for (int i = 0; i < fixedColumnKeys.size(); i++) {
					String fk = fixedColumnKeys.get(i);
					Object value = ClassUtils.getFieldValue(t, fk);
					pivotedItem.setFixedValue(fk, value);
				}

				// extract the useful values from this row
				Object colKeyValue = ClassUtils.getFieldValue(t, columnKeyProperty);
				for (int i = 0; i < pivotedProperties.size(); i++) {
					String key = pivotedProperties.get(i);
					Object value = ClassUtils.getFieldValue(t, key);
					pivotedItem.setValue(colKeyValue, key, value);
				}

			}
		}

		// add last item
		if (pivotedItem != null && result.size() < query.getLimit()) {
			result.add(pivotedItem);
		}

		return result.stream();
	}

	public Consumer<Integer> getAfterCountCompleted() {
		return afterCountCompleted;
	}

	public String getColumnKeyProperty() {
		return columnKeyProperty;
	}

	public List<String> getFixedColumnKeys() {
		return fixedColumnKeys;
	}

	public List<String> getPivotedProperties() {
		return pivotedProperties;
	}

	public String getRowKeyProperty() {
		return rowKeyProperty;
	}

	public int getSize() {
		return size;
	}

	@Override
	public boolean isInMemory() {
		return false;
	}

	public void setAfterCountCompleted(Consumer<Integer> afterCountCompleted) {
		this.afterCountCompleted = afterCountCompleted;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int size(Query<PivotedItem, SerializablePredicate<PivotedItem>> query) {
		dataCache.clear();
		offsetMap.clear();
		lastRequestedOffset = 0;
		lastPivotOffset = 0;
		pivotedItem = null;

		// query the underlying provider
		Optional<SerializablePredicate<T>> sp = (Optional) query.getFilter();
		Query<T, SerializablePredicate<T>> newQuery = new Query<>(query.getOffset(), query.getLimit(),
				query.getSortOrders(), null, sp.isPresent() ? sp.get() : null);
		provider.size(newQuery);

		// get the number of pivoted rows
		this.size = sizeSupplier.get();
		if (getAfterCountCompleted() != null) {
			getAfterCountCompleted().accept(size);
		}
		return size;
	}
}
