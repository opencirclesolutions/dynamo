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
import com.vaadin.server.SerializablePredicate;

/**
 * Converts a Vaadin filter into an Open Circle filter
 * 
 * @author bas.rutten
 */
public class FilterConverter<T> implements Converter<SerializablePredicate<T>, com.ocs.dynamo.filter.Filter> {

	/**
	 * Entity model used when converting filters for detail collections
	 */
	private EntityModel<T> entityModel;

	public FilterConverter(EntityModel<T> entityModel) {
		this.entityModel = entityModel;
	}

	@Override
	public Filter convert(SerializablePredicate<T> filter) {
		if (filter == null) {
			return null;
		}

		Filter result = null;

		if (filter instanceof LikePredicate) {
			LikePredicate<T> p = (LikePredicate<T>) filter;
			result = new Like(p.getProperty(), (String) p.getValue(), p.isCaseSensitive());
		} else if (filter instanceof IsNullPredicate) {
			IsNullPredicate<T> p = (IsNullPredicate<T>) filter;
			result = new IsNull(p.getProperty());
		} else if (filter instanceof EqualsPredicate) {
			EqualsPredicate<T> p = (EqualsPredicate<T>) filter;
			result = new Compare.Equal(p.getProperty(), p.getValue());
		} else if (filter instanceof GreaterThanPredicate) {
			GreaterThanPredicate<T> p = (GreaterThanPredicate<T>) filter;
			result = new Compare.Greater(p.getProperty(), p.getValue());
		} else if (filter instanceof LessThanPredicate) {
			LessThanPredicate<T> p = (LessThanPredicate<T>) filter;
			result = new Compare.Less(p.getProperty(), p.getValue());
		} else if (filter instanceof AndPredicate) {
			AndPredicate<T> p = (AndPredicate<T>) filter;
			And and = new And();
			for (SerializablePredicate<T> operand : p.getOperands()) {
				Filter converted = convert(operand);
				if (converted != null) {
					and.getFilters().add(converted);
				}
			}
			result = and;
		} else if (filter instanceof OrPredicate) {
			OrPredicate<T> p = (OrPredicate<T>) filter;
			Or or = new Or();
			for (SerializablePredicate<T> operand : p.getOperands()) {
				Filter converted = convert(operand);
				if (converted != null) {
					or.getFilters().add(converted);
				}
			}
			result = or;
		} else if (filter instanceof GreaterOrEqualPredicate) {
			GreaterOrEqualPredicate<T> p = (GreaterOrEqualPredicate<T>) filter;
			result = new Compare.GreaterOrEqual(p.getProperty(), p.getValue());
		} else if (filter instanceof LessOrEqualPredicate) {
			LessOrEqualPredicate<T> p = (LessOrEqualPredicate<T>) filter;
			result = new Compare.LessOrEqual(p.getProperty(), p.getValue());
		} else if (filter instanceof NotPredicate) {
			NotPredicate<T> p = (NotPredicate<T>) filter;
			result = new Not(convert(p.getOperand()));
		} else if (filter instanceof BetweenPredicate) {
			BetweenPredicate<T> p = (BetweenPredicate<T>) filter;
			result = new Between(((BetweenPredicate<T>) filter).getProperty(), p.getFromValue(), p.getToValue());
		} else if (filter instanceof SimpleStringPredicate) {
			SimpleStringPredicate<T> p = (SimpleStringPredicate<T>) filter;
			result = new Like(p.getProperty(), (p.isOnlyMatchPrefix() ? "" : "%") + p.getValue() + "%",
					p.isCaseSensitive());
		} else if (filter instanceof InPredicate) {
			InPredicate<T> p = (InPredicate<T>) filter;
			result = new In(p.getProperty(), p.getValue());
		} else if (filter instanceof ContainsPredicate) {
			ContainsPredicate<T> p = (ContainsPredicate<T>) filter;
			result = new Contains(p.getProperty(), p.getValue());
		} else if (filter instanceof ModuloPredicate) {
			ModuloPredicate<T> p = (ModuloPredicate<T>) filter;
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
