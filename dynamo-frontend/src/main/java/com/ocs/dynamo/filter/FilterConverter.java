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
import com.vaadin.flow.function.SerializablePredicate;

import lombok.RequiredArgsConstructor;

/**
 * Converts a Vaadin predicate into a Dynamo filter
 * 
 * @author bas.rutten
 */
@RequiredArgsConstructor
public class FilterConverter<T> implements Converter<SerializablePredicate<T>, com.ocs.dynamo.filter.Filter> {

	private final EntityModel<T> entityModel;

	private Filter convertAnd(SerializablePredicate<T> filter) {
		AndPredicate<T> p = (AndPredicate<T>) filter;
		And and = new And();
		for (SerializablePredicate<T> operand : p.getOperands()) {
			Filter converted = convert(operand);
			if (converted != null) {
				and.getFilters().add(converted);
			}
		}
		return and;
	}

	private Filter convertOr(SerializablePredicate<T> filter) {
		OrPredicate<T> p = (OrPredicate<T>) filter;
		Or or = new Or();
		for (SerializablePredicate<T> operand : p.getOperands()) {
			Filter converted = convert(operand);
			if (converted != null) {
				or.getFilters().add(converted);
			}
		}
		return or;
	}

	@Override
	public Filter convert(SerializablePredicate<T> filter) {
		if (filter == null) {
			return null;
		}

		Filter result = null;

		if (filter instanceof LikePredicate<T> p) {
			result = new Like(p.getProperty(), (String) p.getValue(), p.isCaseSensitive());
		} else if (filter instanceof IsNullPredicate<T> p) {
			result = new IsNull(p.getProperty());
		} else if (filter instanceof EqualsPredicate<T> p) {
			result = new Compare.Equal(p.getProperty(), p.getValue());
		} else if (filter instanceof GreaterThanPredicate<T> p) {
			result = new Compare.Greater(p.getProperty(), p.getValue());
		} else if (filter instanceof LessThanPredicate<T> p) {
			result = new Compare.Less(p.getProperty(), p.getValue());
		} else if (filter instanceof AndPredicate) {
			result = convertAnd(filter);
		} else if (filter instanceof OrPredicate) {
			result = convertOr(filter);
		} else if (filter instanceof GreaterOrEqualPredicate<T> p) {
			result = new Compare.GreaterOrEqual(p.getProperty(), p.getValue());
		} else if (filter instanceof LessOrEqualPredicate<T> p) {
			result = new Compare.LessOrEqual(p.getProperty(), p.getValue());
		} else if (filter instanceof NotPredicate<T> p) {
			result = new Not(convert(p.getOperand()));
		} else if (filter instanceof BetweenPredicate<T> p) {
			result = new Between(((BetweenPredicate<T>) filter).getProperty(), p.getFromValue(), p.getToValue());
		} else if (filter instanceof SimpleStringPredicate<T> p) {
			result = new Like(p.getProperty(), (p.isOnlyMatchPrefix() ? "" : "%") + p.getValue() + "%",
					p.isCaseSensitive());
		} else if (filter instanceof InPredicate<T> p) {
			result = new In(p.getProperty(), p.getValue());
		} else if (filter instanceof ContainsPredicate<T> p) {
			result = new Contains(p.getProperty(), p.getValue());
		} else if (filter instanceof ModuloPredicate<T> p) {
			if (p.getModExpression() != null) {
				result = new Modulo(p.getProperty(), p.getModExpression(), p.getValue());
			} else {
				result = new Modulo(p.getProperty(), p.getModValue(), p.getValue());
			}
		}

		// replace any filters for searching detail fields by Contains-filters
		if (entityModel != null) {
			DynamoFilterUtil.replaceMasterAndDetailFilters(result, entityModel);
		}

		return result;
	}

}
