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
package com.ocs.dynamo.ui;

import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Interface used to indicate that the data provider used by the component can
 * be shared by multiple components (e.g. inside an editable grid)
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the objects managed by the provider
 */
public interface SharedProvider<T> {

	/**
	 * 
	 * @return the shared data provider
	 */
	DataProvider<T, SerializablePredicate<T>> getSharedProvider();
}
