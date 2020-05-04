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

import com.ocs.dynamo.utils.ClassUtils;

public class ModuloPredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = 6955667568104108931L;

	private final String modExpression;

	private final Integer modValue;

	public ModuloPredicate(final String property, final String modExpression, final Number result) {
		super(property, result);
		this.modExpression = modExpression;
		this.modValue = null;
	}

	public ModuloPredicate(final String property, final Integer modValue, final Number result) {
		super(property, result);
		this.modExpression = null;
		this.modValue = modValue;
	}

	@Override
	public Number getValue() {
		return (Number) super.getValue();
	}

	public String getModExpression() {
		return modExpression;
	}

	public Integer getModValue() {
		return modValue;
	}

	@Override
	public boolean test(final T t) {
		if (t == null) {
			return false;
		}
		Object value = ClassUtils.getFieldValue(t, getProperty());
		if (value == null) {
			return false;
		}
		if (!Number.class.isAssignableFrom(value.getClass())) {
			return false;
		}

		long temp = ((Number) value).longValue();

		long modVal = 0;
		if (getModExpression() != null) {
			modVal = ((Number) ClassUtils.getFieldValue(t, getModExpression())).longValue();
		} else {
			modVal = modValue.longValue();
		}

		if (modVal == 0L) {
			throw new IllegalArgumentException("Modulo operator cannot be used with '0' as its second argument");
		}

		return temp % modVal == getValue().longValue();
	}
}
