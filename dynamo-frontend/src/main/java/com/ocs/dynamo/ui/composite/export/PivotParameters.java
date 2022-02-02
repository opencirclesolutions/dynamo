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
package com.ocs.dynamo.ui.composite.export;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.ocs.dynamo.ui.provider.PivotAggregationType;

import lombok.Builder;
import lombok.Getter;

/**
 * Parameter object that contains data about how to export pivoted data
 * 
 * @author Bas Rutten
 *
 */
@Getter
@Builder(toBuilder = true)
public class PivotParameters {

	/**
	 * The property that uniquely identifies a row in the pivoted data set
	 */
	private final String rowKeyProperty;

	private final String columnKeyProperty;

	private final List<String> fixedColumnKeys;

	private final List<String> pivotedProperties;

	private final List<String> hiddenPivotedProperties;

	@Builder.Default
	private final Map<String, PivotAggregationType> aggregationMap = new HashMap<>();

	private final List<Object> possibleColumnKeys;

	@Builder.Default
	private final Function<String, String> fixedHeaderMapper = Function.identity();

	@Builder.Default
	private final BiFunction<Object, Object, String> headerMapper = (a, b) -> a.toString();

	private final boolean includeAggregateRow;

	@Builder.Default
	private final Map<String, Class<?>> aggregationClassMap = new HashMap<>();

	public String getRowKeyProperty() {
		return rowKeyProperty;
	}

}
