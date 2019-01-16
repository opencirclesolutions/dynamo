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

/**
 * An interface for user interface components that support the use of a data provider
 * that is shared between multiple instances of a component
 */
import com.vaadin.data.provider.ListDataProvider;

public interface SharedProvider<T> {

	/**
	 * 
	 * @return the shared data provider
	 */
	public ListDataProvider<T> getSharedProvider();
}
