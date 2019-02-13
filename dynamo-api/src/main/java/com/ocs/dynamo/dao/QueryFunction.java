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
package com.ocs.dynamo.dao;

/**
 * Used to specify (aggregate) functions on queries
 * 
 * @author patrick.deenen
 *
 */
public enum QueryFunction {
	/**
	 * Aggregate function for average on a property
	 */
	AF_AVG,

	/**
	 * Aggregate function for sum on a property
	 */
	AF_SUM,

	/**
	 * Aggregate function for count distinct on a property
	 */
	AF_COUNT_DISTINCT,

	/**
	 * Aggregate function for count on a property
	 */
	AF_COUNT;

	public String with(String property) {
		return property + "." + name();
	}
}
