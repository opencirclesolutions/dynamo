package com.ocs.dynamo.filter;

import org.springframework.core.convert.converter.Converter;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;

/**
 * Converts a Vaadin filter into an Open Circle filter
 * 
 * @author bas.rutten
 * 
 */
public class FilterConverter implements Converter<Filter, com.ocs.dynamo.filter.Filter> {

	@Override
	public com.ocs.dynamo.filter.Filter convert(Filter filter) {
		if (filter == null) {
			return null;
		}

		com.ocs.dynamo.filter.Filter result = null;
		if (filter instanceof And) {
			com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And();
			result = and;
			for (Filter f : ((And) filter).getFilters()) {
				and.getFilters().add(convert(f));
			}
			return result;
		} else if (filter instanceof Or) {
			com.ocs.dynamo.filter.Or or = new com.ocs.dynamo.filter.Or();
			result = or;
			for (Filter f : ((Or) filter).getFilters()) {
				or.getFilters().add(convert(f));
			}
			return result;
		} else if (filter instanceof Not) {
			final Not not = (Not) filter;
			return new com.ocs.dynamo.filter.Not(convert(not.getFilter()));
		} else if (filter instanceof Between) {
			final Between between = (Between) filter;
			return new com.ocs.dynamo.filter.Between(between.getPropertyId().toString(),
					between.getStartValue(), between.getEndValue());
		} else if (filter instanceof Compare) {
			final Compare compare = (Compare) filter;
			switch (compare.getOperation()) {
			case EQUAL:
				return new com.ocs.dynamo.filter.Compare.Equal(compare.getPropertyId().toString(),
						compare.getValue());
			case GREATER:
				return new com.ocs.dynamo.filter.Compare.Greater(compare.getPropertyId().toString(),
						compare.getValue());
			case GREATER_OR_EQUAL:
				return new com.ocs.dynamo.filter.Compare.GreaterOrEqual(compare.getPropertyId().toString(),
						compare.getValue());
			case LESS:
				return new com.ocs.dynamo.filter.Compare.Less(compare.getPropertyId().toString(),
						compare.getValue());
			case LESS_OR_EQUAL:
				return new com.ocs.dynamo.filter.Compare.LessOrEqual(compare.getPropertyId().toString(),
						compare.getValue());
			default:
				return null;
			}
		} else if (filter instanceof IsNull) {
			final IsNull isNull = (IsNull) filter;
			return new com.ocs.dynamo.filter.IsNull(isNull.getPropertyId().toString());
		} else if (filter instanceof Like) {
			final Like like = (Like) filter;
			return new com.ocs.dynamo.filter.Like(like.getPropertyId().toString(), like.getValue(),
					like.isCaseSensitive());
		} else if (filter instanceof SimpleStringFilter) {
			final SimpleStringFilter like = (SimpleStringFilter) filter;
			return new com.ocs.dynamo.filter.Like(like.getPropertyId().toString(),
					(like.isOnlyMatchPrefix() ? "" : "%") + like.getFilterString() + "%",
					!like.isIgnoreCase());
		} else {
			throw new UnsupportedOperationException("Filter: " + filter.getClass().getName()
					+ " is not supported.");
		}
	}

}
