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

import com.ocs.dynamo.domain.model.ChartEntityModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.annotation.Chart;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

/**
 * @author patrickdeenen
 *
 */
public class ChartEntityModelFactory implements EntityModelFactory, EntityModelConstruct {

	@Autowired(required = false)
	private MessageService messageService;

	/**
	 * Construct the factory
	 * 
	 */
	public ChartEntityModelFactory() {
		super();
	}

	/**
	 * Construct the factory
	 * 
	 * @param messageService
	 */
	public ChartEntityModelFactory(MessageService messageService) {
		super();
		this.messageService = messageService;
	}

	/* (non-Javadoc)
	 * @see com.ocs.dynamo.domain.model.EntityModelFactory#getModel(java.lang.Class)
	 */
	@Override
	public <T> EntityModel<T> getModel(Class<T> entityClass) {
		return getModel(entityClass.getSimpleName(), entityClass);
	}

	/* (non-Javadoc)
	 * @see com.ocs.dynamo.domain.model.EntityModelFactory#getModel(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> EntityModel<T> getModel(String reference, Class<T> entityClass) {
		ChartEntityModelImpl<T> gem = null;
		Chart ga = entityClass.getAnnotation(Chart.class);
		if (ga != null) {

			// Get properties from chart annotation
			gem = new ChartEntityModelImpl<>();
			gem.setSubTitle(ga.subTitle());
			gem.setSeriesPath(ga.seriesPath());
			gem.setNamePath(ga.namePath());
			gem.setDataPath(ga.dataPath());
			gem.setTooltip(ga.tooltip());

			// Override properties from property file
			String prop = getEntityMessage(reference, ChartEntityModel.SUBTITLE);
			if (!StringUtils.isEmpty(prop)) {
				gem.setSubTitle(prop);
			}
			prop = getEntityMessage(reference, ChartEntityModel.SERIES_PATH);
			if (!StringUtils.isEmpty(prop)) {
				gem.setSeriesPath(prop);
			}
			prop = getEntityMessage(reference, ChartEntityModel.NAME_PATH);
			if (!StringUtils.isEmpty(prop)) {
				gem.setNamePath(prop);
			}
			prop = getEntityMessage(reference, ChartEntityModel.DATA_PATH);
			if (!StringUtils.isEmpty(prop)) {
				gem.setDataPath(prop);
			}
			prop = getEntityMessage(reference, ChartEntityModel.TOOLTIP);
			if (!StringUtils.isEmpty(prop)) {
				gem.setTooltip(prop);
			}
		}
		return gem;
	}

	protected String getEntityMessage(String reference, String propertyName) {
		if (messageService != null) {
			return messageService.getEntityMessage(reference, propertyName, getLocale());
		}
		return null;
	}

	protected Locale getLocale() {
		return new Locale(SystemPropertyUtils.getDefaultLocale());
	}

	@Override
	public EntityModel<?> constructNestedEntityModel(EntityModelFactory master, Class<?> type, String reference) {
		return new LazyChartEntityModelWrapper<>(master, reference, type);
	}

	@Override
	public <T> boolean canProvideModel(String reference, Class<T> entityClass) {
		return entityClass != null && entityClass.getAnnotation(Chart.class) != null;
	}
}
