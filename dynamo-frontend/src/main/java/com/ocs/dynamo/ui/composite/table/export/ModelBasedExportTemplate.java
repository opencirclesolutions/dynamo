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
package com.ocs.dynamo.ui.composite.table.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Template for exporting a data set to Excel based on the Entity model
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public abstract class ModelBasedExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseExportTemplate<ID, T> {

	/**
	 * The entity model used to govern the export
	 */
	private EntityModel<T> entityModel;

	/**
	 * Constructor
	 *
	 * @param service
	 *            the service used to retrieve the data
	 * @param entityModel
	 *            the entity model
	 * @param sortOrders
	 *            any sort orders to apply to the data
	 * @param filter
	 *            the filter that is used to retrieve the appropriate data
	 * @param title
	 *            the title of the sheet
	 * @param customGenerator
	 *            custom style generator
	 * @param joins
	 */
	public ModelBasedExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, SortOrder[] sortOrders,
			Filter filter, String title, boolean intThousandsGrouping, CustomXlsStyleGenerator<ID, T> customGenerator,
			FetchJoinInformation... joins) {
		super(service, sortOrders, filter, title, intThousandsGrouping, customGenerator, joins);
		this.entityModel = entityModel;
	}

	@Override
	protected byte[] generateXls(DataSetIterator<ID, T> iterator) throws IOException {
		setWorkbook(createWorkbook(iterator.size()));
		Sheet sheet = getWorkbook().createSheet(getTitle());
		setGenerator(createGenerator(getWorkbook()));

		boolean resize = canResize();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		// add header row
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(TITLE_ROW_HEIGHT);

		int i = 0;
		for (AttributeModel am : entityModel.getAttributeModels()) {
			if (show(am)) {
				if (!resize) {
					sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
				}
				Cell cell = titleRow.createCell(i);
				cell.setCellStyle(getGenerator().getHeaderStyle(i));
				cell.setCellValue(am.getDisplayName());
				i++;
			}
		}

		// iterate over the rows
		int rowIndex = 1;
		T entity = iterator.next();
		while (entity != null) {
			Row row = sheet.createRow(rowIndex);

			int colIndex = 0;
			for (AttributeModel am : entityModel.getAttributeModels()) {
				if (am != null && show(am)) {
					Object value = ClassUtils.getFieldValue(entity, am.getPath());
					Cell cell = createCell(row, colIndex, entity, value, am);
					writeCellValue(cell, value, entityModel, am);
					colIndex++;
				}
			}
			rowIndex++;
			entity = iterator.next();
		}
		resizeColumns(sheet);

		getWorkbook().write(stream);
		return stream.toByteArray();
	}

	/**
	 * Check whether a certain attribute model must be included in the export
	 *
	 * @param am
	 *            the attribute model
	 * @return <code>true</code> if the attribute must be included,
	 *         <code>false</code> otherwise
	 */
	private boolean show(AttributeModel am) {
		// never show invisible or LOB attributes
		if (!am.isVisible() || AttributeType.LOB.equals(am.getAttributeType())) {
			return false;
		}

		// show multiple value attributes only if they would normally show up in
		// a table
		if (AttributeType.DETAIL.equals(am.getAttributeType())
				|| AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())) {
			return am.isVisibleInTable();
		}
		return true;
	}

	@Override
	protected byte[] generateCsv(DataSetIterator<ID, T> iterator) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, DynamoConstants.UTF_8),
						SystemPropertyUtils.getExportCsvSeparator().charAt(0))) {

			// add header row
			List<String> headers = new ArrayList<>();
			for (AttributeModel am : entityModel.getAttributeModels()) {
				if (show(am)) {
					headers.add(am.getDisplayName());
				}
			}
			writer.writeNext(headers.toArray(new String[0]));

			// iterate over the rows
			T entity = iterator.next();
			while (entity != null) {
				List<String> row = new ArrayList<>();
				for (AttributeModel am : entityModel.getAttributeModels()) {
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
