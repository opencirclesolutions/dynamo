package com.ocs.dynamo.ui.component;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.filter.LikePredicate;
import com.ocs.dynamo.service.BaseService;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;

public class CallbackProviderHelper {

	public static <ID extends Serializable, T extends AbstractEntity<ID>> CallbackDataProvider<T, String> createCallbackProvider(
			BaseService<ID, T> service, EntityModel<T> entityModel, SerializablePredicate<T> filter) {
		FilterConverter<T> converter = new FilterConverter<T>(entityModel);
		CallbackDataProvider<T, String> callbackProvider = new CallbackDataProvider<>(query -> {
			int offset = query.getOffset();
			int page = offset / query.getLimit();

			SerializablePredicate<T> pred = constructFilterPredicate(query, entityModel, filter);
			return service.fetch(converter.convert(pred), page, query.getLimit()).stream();
		}, query -> {
			SerializablePredicate<T> pred = constructFilterPredicate(query, entityModel, filter);
			return (int) service.count(converter.convert(pred), true);
		});
		return callbackProvider;
	}

	private static <ID extends Serializable, T extends AbstractEntity<ID>> SerializablePredicate<T> constructFilterPredicate(
			Query<T, String> query, EntityModel<T> entityModel, SerializablePredicate<T> filter) {
		String searchString = query.getFilter().orElse(null);

		SerializablePredicate<T> pred = null;
		SerializablePredicate<T> like = new LikePredicate<>(entityModel.getFilterProperty(), "%" + searchString + "%",
				false);

		if (filter == null) {
			if (!StringUtils.isEmpty(searchString)) {
				pred = like;
			}
		} else {
			if (!StringUtils.isEmpty(searchString)) {
				pred = new AndPredicate<T>(filter, like);
			} else {
				pred = filter;
			}
		}

		return pred;
	}
}
