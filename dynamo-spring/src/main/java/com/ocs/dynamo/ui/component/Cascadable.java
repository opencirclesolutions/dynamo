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
package com.ocs.dynamo.ui.component;

import com.vaadin.data.Container.Filter;

/**
 * Interface for selection fields that support cascading search (i.e. they change their selection
 * options in response to a value change of another field)
 * 
 * @author bas.rutten
 *
 */
public interface Cascadable {

	/**
	 * Add the provided filter to the current filter for the component. Used for cascading
	 * 
	 * @param additionalFilter
	 *            the additional filter to set
	 */
	void setAdditionalFilter(Filter additionalFilter);

	/**
	 * Removes the additional filter
	 */
	void clearAdditionalFilter();
}
