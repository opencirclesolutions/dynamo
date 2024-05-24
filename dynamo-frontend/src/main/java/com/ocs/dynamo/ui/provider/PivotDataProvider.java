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
import java.util.Collections;
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

import lombok.Getter;
import lombok.Setter;

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
	@Getter
	@Setter
	private Consumer<Integer> afterCountCompleted;

	/**
	 * The name of the property that contains the identifying value for a column
	 */
	@Getter
	private final String columnKeyProperty;

	/**
	 * Cache for keeping track of current page of data
	 */
	private final Queue<T> dataCache = new LinkedList<>();

	@Getter
	private final List<String> fixedColumnKeys;

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
	private Object lastRowKeyValue;

	/**
	 * Mapping from requested offset to offset in the wrapped provider
	 */
	private final Map<Integer, Integer> offsetMap = new HashMap<>();

	/**
	 * The pivoted item that is currently being constructed
	 */
	private PivotedItem pivotedItem;

	/**
	 * The list of pivoted properties
	 */
	@Getter
	private final List<String> pivotedProperties;

	/**
	 * The list of hidden pivoted properties
	 */
	@Getter
	private final List<String> hiddenPivotedProperties;

	/**
	 * The data provider that is being wrapped
	 */
	private final BaseDataProvider<ID, T> provider;

	/**
	 * The property that is checked to see if a new row has been reached
	 */
	@Getter
	private final String rowKeyProperty;

	@Getter
	private int size;

	/**
	 * Supplier to carry out to retrieve size of pivoted data set
	 */
	private final Supplier<Integer> sizeSupplier;

	@Setter
	private Map<String, PivotAggregationType> aggregationMap = new HashMap<>();

	@Setter
	private Map<String, Class<?>> aggregationClassMap = new HashMap<>();

	/**
	 * Constructor
	 * @param provider                the wrapped data provider
	 * @param rowKeyProperty          the property to check for unique row values
	 * @param columnKeyProperty       the property to check for the column key
	 * @param fixedColumnKeys         the fixed columns
	 * @param pivotedProperties       the pivoted properties
	 * @param hiddenPivotedProperties the hidden pivoted properties
	 * @param sizeSupplier            supplier that is called to determine the
	 *                                number of rows in the result set
	 */
	public PivotDataProvider(BaseDataProvider<ID, T> provider, String rowKeyProperty, String columnKeyProperty,
			List<String> fixedColumnKeys, List<String> pivotedProperties, List<String> hiddenPivotedProperties,
			Supplier<Integer> sizeSupplier) {
		this.provider = provider;
		this.columnKeyProperty = columnKeyProperty;
		this.rowKeyProperty = rowKeyProperty;
		this.fixedColumnKeys = fixedColumnKeys;
		this.pivotedProperties = pivotedProperties;
		this.hiddenPivotedProperties = hiddenPivotedProperties == null ? Collections.emptyList()
				: hiddenPivotedProperties;
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

			Optional<SerializablePredicate<T>> predicate = (Optional) query.getFilter();

			// re-load earlier page
			if (requestedOffset < lastRequestedOffset && offsetMap.containsKey(requestedOffset)) {
				dataCache.clear();
				lastPivotOffset = offsetMap.get(requestedOffset);
				pivotedItem = null;
			}

			lastRequestedOffset = requestedOffset;

			fetchNextPageIfNeeded(query, requestedOffset, predicate.orElse(null));

			if (dataCache.isEmpty()) {
				// no more records left before end of page, abort
				break;
			} else {
				handleRow(result);
			}
		}

		// add last item
		if (pivotedItem != null && result.size() < query.getLimit()) {
			result.add(pivotedItem);
		}

		return result.stream();
	}
	
	@Override
	public boolean isInMemory() {
		return false;
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

	/**
	 * Adds an aggregation of the specified type for the specified property
	 * @param pivotProperty the property to aggregate on 
	 * @param type the type of aggregation (e.g. sum, count)
	 * @param clazz the data type of the aggregation
	 */
	public void addAggregation(String pivotProperty, PivotAggregationType type, Class<?> clazz) {
		aggregationMap.put(pivotProperty, type);
		aggregationClassMap.put(pivotProperty, clazz);
	}

	public PivotAggregationType getAggregation(String pivotProperty) {
		return aggregationMap.get(pivotProperty);
	}

	public Class<?> getAggregationClass(String pivotProperty) {
		return aggregationClassMap.get(pivotProperty);
	}

	public List<String> getAllPrivotProperties() {
		List<String> allProps = new ArrayList<>();
		allProps.addAll(getPivotedProperties());
		allProps.addAll(getHiddenPivotedProperties());
		return allProps;
	}

	/**
	 * Handles a single row from the underlying result set
	 * 
	 * @param result the current list of pivoted items
	 */
	private void handleRow(List<PivotedItem> result) {
		T entity = dataCache.poll();

		// get the row value to determine if we need a new row
		Object rowKeyValue = ClassUtils.getFieldValue(entity, rowKeyProperty);

		// create new pivoted item if needed and add existing one to result set
		if (lastRowKeyValue == null || !Objects.equals(rowKeyValue, lastRowKeyValue)) {
			if (pivotedItem != null) {
				result.add(pivotedItem);
			}
			pivotedItem = new PivotedItem(rowKeyValue);
			lastRowKeyValue = rowKeyValue;
		}

		// add fixed columns
		for (String fixedColumnKey : fixedColumnKeys) {
			Object value = ClassUtils.getFieldValue(entity, fixedColumnKey);
			pivotedItem.setFixedValue(fixedColumnKey, value);
		}

		// extract the useful values from this row
		Object colKeyValue = ClassUtils.getFieldValue(entity, columnKeyProperty);
		for (String propertyName : pivotedProperties) {
			Object value = ClassUtils.getFieldValue(entity, propertyName);
			pivotedItem.setValue(colKeyValue, propertyName, value);
		}

		// extract additional useful (but invisible) values
		if (hiddenPivotedProperties != null) {
			for (String propertyName : hiddenPivotedProperties) {
				Object value = ClassUtils.getFieldValue(entity, propertyName);
				pivotedItem.setValue(colKeyValue, propertyName, value);
			}
		}
	}

	/**
	 * Fetches the next page of data from the underlying provider if needed
	 * 
	 * @param query           the incoming query object
	 * @param requestedOffset the requested offset
	 * @param predicate       the filter predicate
	 */
	private void fetchNextPageIfNeeded(Query<PivotedItem, SerializablePredicate<PivotedItem>> query, int requestedOffset,
									   SerializablePredicate<T> predicate) {
		if (dataCache.isEmpty()) {
			offsetMap.put(requestedOffset, lastPivotOffset);
			Query<T, SerializablePredicate<T>> newQuery = new Query<>(lastPivotOffset, PAGE_SIZE, query.getSortOrders(),
					null, predicate);
			provider.fetch(newQuery).forEach(dataCache::add);
			lastPivotOffset = lastPivotOffset + PAGE_SIZE;
		}
	}


}
