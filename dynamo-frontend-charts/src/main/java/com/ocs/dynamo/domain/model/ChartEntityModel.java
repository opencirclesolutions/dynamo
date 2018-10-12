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

/**
 * The interface for chart entity models
 * 
 * @author patrickdeenen
 *
 * @param <T>
 */
public interface ChartEntityModel<T> extends EntityModel<T> {

	String SUBTITLE = "subTitle";
	String TOOLTIP = "tooltip";
	String SERIES_PATH = "seriesPath";
	String NAME_PATH = "namePath";
	String DATA_PATH = "dataPath";

	/**
	 * @return the subTitle
	 */
	String getSubTitle();

	/**
	 * @return the tooltip
	 */
	String getTooltip();

	/**
	 * @return the series
	 */
	AttributeModel getSeriesAttributeModel();

	/**
	 * @return the name attribute model
	 */
	AttributeModel getNameAttributeModel();

	/**
	 * @return the dataPath
	 */
	AttributeModel getDataAttributeModel();

}
