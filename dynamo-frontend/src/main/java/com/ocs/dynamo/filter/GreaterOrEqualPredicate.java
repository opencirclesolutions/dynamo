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

/**
 * A predicate that is used to indicate that a value is greater than the
 * provided value
 * 
 * @author Bas Rutten
 *
 * @param <T> the type parameter
 */
public class GreaterOrEqualPredicate<T> extends ComparePredicate<T> {

	private static final long serialVersionUID = 116403947130693521L;

	public GreaterOrEqualPredicate(String property, Object value) {
		super(property, value);
	}

	@Override
	public boolean test(T t) {
		if (t == null) {
			return false;
		}
		Object v = com.ocs.dynamo.utils.ClassUtils.getFieldValue(t, getProperty());
		return compareValue(v) >= 0;
	}

}
