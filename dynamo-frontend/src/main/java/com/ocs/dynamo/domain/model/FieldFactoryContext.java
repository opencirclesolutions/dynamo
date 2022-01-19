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
package com.ocs.dynamo.domain.model;

import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Builder;
import lombok.Data;

/**
 * Field factory context that can be used to specify how the FieldFactory must
 * construct a field
 * 
 * @author Bas Rutten
 *
 */
@Data
@Builder(toBuilder = true)
public class FieldFactoryContext {

	/**
	 * Creates a blank context
	 * 
	 * @return
	 */
	public static FieldFactoryContext.FieldFactoryContextBuilder create() {
		return FieldFactoryContext.builder();
	}

	/**
	 * Creates a default context based on only the attribute model
	 * 
	 * @param am the attribute model
	 * @return
	 */
	public static FieldFactoryContext createDefault(AttributeModel am) {
		return FieldFactoryContext.builder().attributeModel(am).build();
	}

	private AttributeModel attributeModel;

	private boolean editableGrid;

	private EntityModel<?> fieldEntityModel;

	private Map<String, SerializablePredicate<?>> fieldFilters;

	private AbstractEntity<?> parentEntity;

	private boolean search;

	private Map<String, DataProvider<?, SerializablePredicate<?>>> sharedProviders;

	private boolean viewMode;

	public void addSharedProvider(String attribute,
			DataProvider<? extends AbstractEntity<?>, SerializablePredicate<?>> sharedProvider) {
		sharedProviders.put(attribute, sharedProvider);
	}

	public DataProvider<?, SerializablePredicate<?>> getSharedProvider(String attribute) {
		return sharedProviders == null ? null : sharedProviders.get(attribute);
	}

}
