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
package com.ocs.dynamo.filter;

import org.springframework.core.convert.converter.Converter;

import com.ocs.dynamo.domain.model.EntityModel;
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
 */
public class FilterConverter implements Converter<Filter, com.ocs.dynamo.filter.Filter> {

	/**
	 * Entity model used when converting filters for detail collections
	 */
	private EntityModel<?> entityModel;

	public FilterConverter(EntityModel<?> entityModel) {
		this.entityModel = entityModel;
	}

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
				com.ocs.dynamo.filter.Filter converted = convert(f);
				if (converted != null) {
					and.getFilters().add(converted);
				}
			}
		} else if (filter instanceof Or) {
			com.ocs.dynamo.filter.Or or = new com.ocs.dynamo.filter.Or();
			result = or;
			for (Filter f : ((Or) filter).getFilters()) {
				or.getFilters().add(convert(f));
			}
		} else if (filter instanceof Not) {
			final Not not = (Not) filter;
			result = new com.ocs.dynamo.filter.Not(convert(not.getFilter()));
		} else if (filter instanceof Between) {
			final Between between = (Between) filter;
			result = new com.ocs.dynamo.filter.Between(between.getPropertyId().toString(), between.getStartValue(),
					between.getEndValue());
		} else if (filter instanceof Compare) {
			final Compare compare = (Compare) filter;
			switch (compare.getOperation()) {
			case EQUAL:
				result = new com.ocs.dynamo.filter.Compare.Equal(compare.getPropertyId().toString(),
						compare.getValue());
				break;
			case GREATER:
				result = new com.ocs.dynamo.filter.Compare.Greater(compare.getPropertyId().toString(),
						compare.getValue());
				break;
			case GREATER_OR_EQUAL:
				result = new com.ocs.dynamo.filter.Compare.GreaterOrEqual(compare.getPropertyId().toString(),
						compare.getValue());
				break;
			case LESS:
				result = new com.ocs.dynamo.filter.Compare.Less(compare.getPropertyId().toString(), compare.getValue());
				break;
			case LESS_OR_EQUAL:
				result = new com.ocs.dynamo.filter.Compare.LessOrEqual(compare.getPropertyId().toString(),
						compare.getValue());
				break;
			default:
				result = null;
			}
		} else if (filter instanceof IsNull) {
			final IsNull isNull = (IsNull) filter;
			result = new com.ocs.dynamo.filter.IsNull(isNull.getPropertyId().toString());
		} else if (filter instanceof Like) {
			final Like like = (Like) filter;
			result = new com.ocs.dynamo.filter.Like(like.getPropertyId().toString(), like.getValue(),
					like.isCaseSensitive());
		} else if (filter instanceof SimpleStringFilter) {
			final SimpleStringFilter like = (SimpleStringFilter) filter;
			result = new com.ocs.dynamo.filter.Like(like.getPropertyId().toString(),
					(like.isOnlyMatchPrefix() ? "" : "%") + like.getFilterString() + "%", !like.isIgnoreCase());
		} else {
			throw new UnsupportedOperationException("Filter: " + filter.getClass().getName() + " is not supported.");
		}

		// replace any filters for searching detail fields by Contains-filters
		if (entityModel != null) {
			DynamoFilterUtil.replaceMasterAndDetailFilters(result, entityModel);
		}

		return result;
	}

}
