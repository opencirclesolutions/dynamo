package org.dynamoframework.domain.query;

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

import org.dynamoframework.domain.AbstractEntity;

import java.io.Serializable;
import java.util.List;

/**
 * A simple iterator for iterating over a fixed set of data - this basically
 * just delegates to the underlying collection
 *
 * @param <ID> the ID of the entity to iterate over
 * @param <T>  the type of the entity to iterate over
 * @author Bas Rutten
 */
public class FixedDataSetIterator<ID extends Serializable, T extends AbstractEntity<ID>> implements DataSetIterator<ID, T> {

	private final List<T> items;

	private int index = 0;

	public FixedDataSetIterator(List<T> items) {
		this.items = items;
	}

	@Override
	public T next() {
		if (index < items.size()) {
			return items.get(index++);
		}
		return null;
	}

	@Override
	public int size() {
		return items.size();
	}

}
