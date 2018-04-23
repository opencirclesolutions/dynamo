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
package com.ocs.dynamo.domain.model.impl;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.ChartEntityModel;

/**
 * Extension of the entitymodel to support charts
 * 
 * @author patrickdeenen
 *
 */
public class ChartEntityModelImpl<T> extends EntityModelImpl<T> implements ChartEntityModel<T> {

	private String subTitle;
	private String tooltip;
	private String seriesPath;
	private String namePath;
	private String dataPath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.domain.model.impl.ChartEntityModel#getSubTitle()
	 */
	@Override
	public String getSubTitle() {
		return subTitle;
	}

	/**
	 * @param subTitle
	 *            the subTitle to set
	 */
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.domain.model.impl.ChartEntityModel#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @param tooltip
	 *            the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @return the path to the series
	 */
	public String getSeriesPath() {
		return seriesPath;
	}

	/**
	 * @param seriesPath
	 *            the seriesPath to set
	 */
	public void setSeriesPath(String seriesPath) {
		this.seriesPath = seriesPath;
	}

	/**
	 * @return the path to the name
	 */
	public String getNamePath() {
		return namePath;
	}

	/**
	 * @param namePath
	 *            the namePath to set
	 */
	public void setNamePath(String namePath) {
		this.namePath = namePath;
	}

	/**
	 * @return the path to the data
	 */
	public String getDataPath() {
		return dataPath;
	}

	/**
	 * @param dataPath
	 *            the dataPath to set
	 */
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.domain.model.impl.ChartEntityModel#getSeriesAttributeModel()
	 */
	@Override
	public AttributeModel getSeriesAttributeModel() {
		return getAttributeModel(seriesPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.domain.model.impl.ChartEntityModel#getNameAttributeModel()
	 */
	@Override
	public AttributeModel getNameAttributeModel() {
		return getAttributeModel(namePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.domain.model.impl.ChartEntityModel#getDataAttributeModel()
	 */
	@Override
	public AttributeModel getDataAttributeModel() {
		return getAttributeModel(dataPath);
	}
}
