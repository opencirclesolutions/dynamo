package org.dynamoframework.export.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.poi.util.LocaleUtil;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.export.CustomXlsStyleGenerator;
import org.dynamoframework.export.ExportService;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.Filter;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.ServiceLocatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Implementation of the export service
 *
 * @author Bas Rutten
 */
@Service
public class ExportServiceImpl implements ExportService {

	@Autowired
	private DynamoProperties dynamoProperties;

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportCsv(EntityModel<T> entityModel,
																					ExportMode mode, Filter filter, List<SortOrder> sortOrders,
																					Locale locale,
																					FetchJoinInformation... joins) {
		BaseService<ID, T> service = (BaseService<ID, T>) ServiceLocatorFactory.getServiceLocator()
			.getServiceForEntity(entityModel.getEntityClass());

		ModelBasedCsvExportTemplate<ID, T> template = new ModelBasedCsvExportTemplate<>(dynamoProperties, service, entityModel, mode,
			sortOrders, filter, locale, joins);
		return template.process();
	}

	@Override
	@Transactional
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportCsvFixed(EntityModel<T> entityModel,
																						 ExportMode mode, List<T> items,
																						 Locale locale) {
		ModelBasedCsvExportTemplate<ID, T> template = new ModelBasedCsvExportTemplate<>(dynamoProperties, null, entityModel, mode, null,
			null, new Locale.Builder().setLanguage("nl").build());
		return template.processFixed(items);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportExcel(EntityModel<T> entityModel,
																					  ExportMode mode, Filter filter, List<SortOrder> sortOrders,
																					  Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, Locale locale, FetchJoinInformation... joins) {
		BaseService<ID, T> service = (BaseService<ID, T>) ServiceLocatorFactory.getServiceLocator()
			.getServiceForEntity(entityModel.getEntityClass());
		ModelBasedExcelExportTemplate<ID, T> template = new ModelBasedExcelExportTemplate<>(dynamoProperties, service, entityModel, mode,
			sortOrders, filter,
			entityModel.getDisplayNamePlural(LocaleUtil.getUserLocale()), customGenerator, locale, joins);
		return template.process();
	}

	@Override
	@Transactional
	public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportExcelFixed(EntityModel<T> entityModel,
																						   ExportMode mode, Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, List<T> items,
																						   Locale locale) {
		ModelBasedExcelExportTemplate<ID, T> template = new ModelBasedExcelExportTemplate<>(dynamoProperties, null, entityModel, mode,
			null, null, entityModel.getDisplayNamePlural(LocaleUtil.getUserLocale()), customGenerator, locale);
		return template.processFixed(items);
	}

}
