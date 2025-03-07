package org.dynamoframework.filter;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;

/**
 * A filter that negates the value of another filter
 *
 * @author bas.rutten
 */
@Setter
@Getter
public final class Not extends AbstractFilter {

	private Filter filter;

	public Not(Filter filter) {
		this.filter = filter;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Not not)) {
			return false;
		}
		return filter.equals(not.getFilter());
	}

	@Override
	public int hashCode() {
		return filter.hashCode();
	}

	@Override
	public boolean evaluate(Object that) {
		return !this.filter.evaluate(that);
	}

	@Override
	public String toString() {
		return super.toString() + " " + getFilter();
	}

}
