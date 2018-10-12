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

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Field;

/**
 * Interface for model based component factories
 * 
 * @author patrickdeenen
 *
 */
public interface FieldFactory {

	/**
	 * The context for field generation, all fields are optional, but one should provide as many as possible.
	 * 
	 * @author patrickdeenen
	 *
	 */
	public interface Context {

		Container getContainer();

		AttributeModel getAttributeModel();

		Map<String, Filter> getFieldFilters();

		<T> EntityModel<T> getFieldEntityModel();

		<P> P getParentEntity();

		Boolean getViewMode();

		Boolean isSearch();

	}

	/**
	 * Constructs a Component based on an attribute model
	 * 
	 * @param context
	 *            the generation context
	 * @return
	 */
	Field<?> constructField(Context context);

}
