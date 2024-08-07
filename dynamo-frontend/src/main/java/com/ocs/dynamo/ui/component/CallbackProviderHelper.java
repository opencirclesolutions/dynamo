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
package com.ocs.dynamo.ui.component;

import java.io.Serializable;
import java.util.List;
import java.util.function.IntConsumer;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.filter.LikePredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.experimental.UtilityClass;

/**
 * Utility class for creating callback providers
 * 
 * @author BasRutten
 *
 */
@UtilityClass
public class CallbackProviderHelper {

	/**
	 * Creates a callback data provider that does not support filtering
	 * 
	 * @param <ID>        the type of the primary key of the entity
	 * @param <T>         the type of the entity
	 * @param service     the service that is used to retrieve entities
	 * @return the callback provider
	 */
	public static <ID extends Serializable, T extends AbstractEntity<ID>> CallbackDataProvider<T, Void> createCallbackProviderNoFiltering(
			BaseService<ID, T> service, SortOrders sortOrders, IntConsumer afterCountDone) {
		return new CallbackDataProvider<>(query -> {
			int offset = query.getOffset();
			int page = offset / query.getLimit();
			List<T> list = service.fetch(null, page, query.getLimit(), sortOrders);
			if (afterCountDone != null) {
				afterCountDone.accept(list.size());
			}
			return list.stream();
		}, query -> {
			try {
				int count = (int) service.count(null, true);
				if (afterCountDone != null) {
					afterCountDone.accept(count);
				}
				return count;
			} catch (Exception ex) {
				VaadinUtils.showErrorNotification(ex.getMessage());
				return 0;
			}
		});
	}
	
	/**
	 * Creates a callback data provider for use with lookup components
	 * 
	 * @param <ID>        the type of the primary key of the entity
	 * @param <T>         the type of the entity
	 * @param service     the service that is used to retrieve entities
	 * @param entityModel the entity model
	 * @param filter      search filter to apply (in addition to the search term)
	 * @return the constructed callback
	 */
	public static <ID extends Serializable, T extends AbstractEntity<ID>> CallbackDataProvider<T, String> createCallbackProvider(
			BaseService<ID, T> service, EntityModel<T> entityModel, SerializablePredicate<T> filter,
			SortOrders sortOrders, IntConsumer afterCountDone) {
		FilterConverter<T> converter = new FilterConverter<>(entityModel);
		return new CallbackDataProvider<>(query -> {
			int offset = query.getOffset();
			int page = offset / query.getLimit();

			SerializablePredicate<T> predicate = constructFilterPredicate(query, entityModel, filter);
			List<T> list = service.fetch(converter.convert(predicate), page, query.getLimit(), sortOrders);
			if (afterCountDone != null) {
				afterCountDone.accept(list.size());
			}
			return list.stream();
		}, query -> {
			try {
				SerializablePredicate<T> predicate = constructFilterPredicate(query, entityModel, filter);
				int count = (int) service.count(converter.convert(predicate), true);
				if (afterCountDone != null) {
					afterCountDone.accept(count);
				}
				return count;
			} catch (Exception ex) {
				VaadinUtils.showErrorNotification(ex.getMessage());
				return 0;
			}
		});
	}

	/**
	 * Creates the predicate that is used for searching (by combining the field
	 * filter with the search term)
	 * 
	 * @param <ID>        the type of the primary key of the entity
	 * @param <T>         the type of the entity
	 * @param query       the query
	 * @param entityModel the entity model
	 * @param filter      the field filter
	 * @return the constructed predicate
	 */
	private static <ID extends Serializable, T extends AbstractEntity<ID>> SerializablePredicate<T> constructFilterPredicate(
			Query<T, String> query, EntityModel<T> entityModel, SerializablePredicate<T> filter) {
		String searchString = query.getFilter().orElse(null);

		SerializablePredicate<T> predicate = null;
		SerializablePredicate<T> like = new LikePredicate<>(entityModel.getFilterProperty(), "%" + searchString + "%",
				false);

		if (filter == null) {
			if (!StringUtils.isEmpty(searchString)) {
				predicate = like;
			}
		} else {
			if (!StringUtils.isEmpty(searchString)) {
				predicate = new AndPredicate<>(filter, like);
			} else {
				predicate = filter;
			}
		}

		return predicate;
	}
	

}
