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
package com.ocs.dynamo.export.impl;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.export.ExportService;
import com.ocs.dynamo.export.type.ExportMode;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import org.apache.poi.util.LocaleUtil;
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

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportCsv(EntityModel<T> entityModel,
                                                                                    ExportMode mode, Filter filter, List<SortOrder> sortOrders,
                                                                                    Locale locale,
                                                                                    FetchJoinInformation... joins) {
        BaseService<ID, T> service = (BaseService<ID, T>) ServiceLocatorFactory.getServiceLocator()
                .getServiceForEntity(entityModel.getEntityClass());

        ModelBasedCsvExportTemplate<ID, T> template = new ModelBasedCsvExportTemplate<>(service, entityModel, mode,
                sortOrders, filter, locale, joins);
        return template.process();
    }

    @Override
    @Transactional
    public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportCsvFixed(EntityModel<T> entityModel,
                                                                                         ExportMode mode, List<T> items,
                                                                                         Locale locale) {
        ModelBasedCsvExportTemplate<ID, T> template = new ModelBasedCsvExportTemplate<>(null, entityModel, mode, null,
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
        ModelBasedExcelExportTemplate<ID, T> template = new ModelBasedExcelExportTemplate<>(service, entityModel, mode,
                sortOrders, filter,
                entityModel.getDisplayNamePlural(LocaleUtil.getUserLocale()), customGenerator, locale, joins);
        return template.process();
    }

    @Override
    @Transactional
    public <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportExcelFixed(EntityModel<T> entityModel,
                                                                                           ExportMode mode, Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, List<T> items,
                                                                                           Locale locale) {
        ModelBasedExcelExportTemplate<ID, T> template = new ModelBasedExcelExportTemplate<>(null, entityModel, mode,
                null, null, entityModel.getDisplayNamePlural(LocaleUtil.getUserLocale()), customGenerator, locale);
        return template.processFixed(items);
    }

}
