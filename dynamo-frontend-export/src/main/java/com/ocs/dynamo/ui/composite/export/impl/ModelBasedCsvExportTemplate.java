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
package com.ocs.dynamo.ui.composite.export.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.opencsv.CSVWriter;

/**
 * A template for exporting data to CSV
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T> the type of the entity to export
 */
public class ModelBasedCsvExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseExportTemplate<ID, T> {

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param exportMode
	 * @param sortOrders
	 * @param filter
	 * @param title
	 * @param intThousandsGrouping
	 * @param joins
	 */
	public ModelBasedCsvExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
			SortOrder[] sortOrders, Filter filter, String title, CustomXlsStyleGenerator<ID, T> customGenerator,
			FetchJoinInformation... joins) {
		super(service, entityModel, exportMode, sortOrders, filter, title, joins);
	}

	@Override
	protected byte[] generate(DataSetIterator<ID, T> iterator) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, DynamoConstants.UTF_8),
						SystemPropertyUtils.getCsvSeparator().charAt(0),
						SystemPropertyUtils.getCsvQuoteChar().charAt(0),
						SystemPropertyUtils.getCsvEscapeChar().charAt(0), String.format("%n"))) {

			// add header row
			List<String> headers = new ArrayList<>();
			for (AttributeModel am : getEntityModel().getAttributeModels()) {
				if (show(am)) {
					headers.add(am.getDisplayName(VaadinUtils.getLocale()));
				}
			}
			writer.writeNext(headers.toArray(new String[0]));

			// iterate over the rows
			T entity = iterator.next();
			while (entity != null) {
				List<String> row = new ArrayList<>();
				for (AttributeModel am : getEntityModel().getAttributeModels()) {
					if (show(am)) {
						Object value = ClassUtils.getFieldValue(entity, am.getPath());
						EntityModelFactory emf = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();
						String str = FormatUtils.formatPropertyValue(emf, am, value, ", ");
						row.add(str);
					}
				}
				if (!row.isEmpty()) {
					writer.writeNext(row.toArray(new String[0]));
				}
				entity = iterator.next();
			}
			writer.flush();
			return out.toByteArray();
		}
	}

}
