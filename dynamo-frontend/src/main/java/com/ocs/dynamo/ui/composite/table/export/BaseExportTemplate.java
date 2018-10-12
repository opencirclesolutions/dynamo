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

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.MathUtils;

/**
 * Base class for entity model based exports to Excel or CSV
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 */
public abstract class BaseExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> {

	/**
	 * The width for a fixed with column
	 */
	protected static final int FIXED_COLUMN_WIDTH = 20 * 256;

	protected static final int TITLE_ROW_HEIGHT = 40;

	/**
	 * The maximum number of rows that will be created normally. If the number
	 * is
	 * larger, then a streaming writer will be used. This is faster but it
	 * will mean
	 * we cannot auto size the columns
	 */
	protected static final int MAX_SIZE = 20000;

	private CustomXlsStyleGenerator<ID, T> customGenerator;

	/**
	 * Whether to use the thousands separator for integers
	 */
	private final boolean intThousandsGrouping;

	/**
	 * Entity model factory
	 */
	private EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

	/**
	 * The filter to use to restrict the search results
	 */
	private final Filter filter;

	/**
	 * The style generator
	 */
	private XlsStyleGenerator<ID, T> generator;

	/**
	 * Custom joins to use
	 */
	private final FetchJoinInformation[] joins;

	/**
	 * Service used to retrieve search results
	 */
	private final BaseService<ID, T> service;

	private final SortOrder[] sortOrders;

	private final String title;

	private Workbook workbook;

	/**
	 * Constructor
	 *
	 * @param service
	 *            the service used to retrieve the data
	 * @param sortOrders
	 *            the sort order
	 * @param filter
	 *            the filter used to limit the data
	 * @param title
	 *            the title of the sheet
	 * @param customGenerator
	 *            custom generator used to apply extra styling
	 * @param joins
	 */
	public BaseExportTemplate(BaseService<ID, T> service, SortOrder[] sortOrders, Filter filter, String title,
			boolean intThousandsGrouping, CustomXlsStyleGenerator<ID, T> customGenerator,
			FetchJoinInformation... joins) {
		this.service = service;
		this.sortOrders = sortOrders;
		this.filter = filter;
		this.title = title;
		this.joins = joins;
		this.intThousandsGrouping = intThousandsGrouping;
		this.customGenerator = customGenerator;
	}

	/**
	 * Indicates whether it is possible to resize the columns
	 *
	 * @return
	 */
	protected boolean canResize() {
		return !(getWorkbook() instanceof SXSSFWorkbook);
	}

	/**
	 * Creates an Excel cell and applies the correct style. The style to use
	 * depends
	 * on the attribute model and the value to display in the cell
	 *
	 * @param row
	 *            the row to which to add the cell
	 * @param colIndex
	 *            the column index of the cell
	 * @param entity
	 *            the entity that is represented in the row
	 * @param value
	 *            the cell value
	 * @param attributeModel
	 *            the attribute model used to determine the style
	 * @return
	 */
	protected Cell createCell(Row row, int colIndex, T entity, Object value, AttributeModel attributeModel) {
		Cell cell = row.createCell(colIndex);
		cell.setCellStyle(getGenerator().getCellStyle(colIndex, entity, value, attributeModel));
		if (getCustomGenerator() != null) {
			CellStyle custom = getCustomGenerator().getCustomCellStyle(workbook, entity, value, attributeModel);
			if (custom != null) {
				cell.setCellStyle(custom);
			}
		}
		return cell;
	}

	/**
	 * Creates the style generator
	 *
	 * @param workbook
	 *            the work book that is being created
	 * @return
	 */
	protected XlsStyleGenerator<ID, T> createGenerator(Workbook workbook) {
		return new BaseXlsStyleGenerator<>(workbook, intThousandsGrouping);
	}

	/**
	 * Creates an appropriate work book - if the size is below the threshold
	 * then a
	 * normal workbook is created. Otherwise a streaming workbook is
	 * created. This
	 * is much faster and more efficient, but you cannot auto
	 * resize the columns
	 *
	 * @param size
	 *            the number of rows
	 * @return
	 */
	protected Workbook createWorkbook(int size) {
		if (size > MAX_SIZE) {
			return new SXSSFWorkbook();
		}
		return new XSSFWorkbook();
	}

	protected abstract byte[] generateCsv(DataSetIterator<ID, T> iterator) throws IOException;

	/**
	 * Generates the Excel (XLS) file
	 *
	 * @param iterator
	 *            data set iterator that contains the rows to include
	 * @return
	 * @throws IOException
	 */
	protected abstract byte[] generateXls(DataSetIterator<ID, T> iterator) throws IOException;

	public CustomXlsStyleGenerator<ID, T> getCustomGenerator() {
		return customGenerator;
	}

	public Filter getFilter() {
		return filter;
	}

	public XlsStyleGenerator<ID, T> getGenerator() {
		return generator;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	/**
	 * Returns the size of a single page of data
	 *
	 * @return
	 */
	public abstract int getPageSize();

	public BaseService<ID, T> getService() {
		return service;
	}

	public SortOrder[] getSortOrders() {
		return sortOrders;
	}

	public String getTitle() {
		return title;
	}

	protected Workbook getWorkbook() {
		return workbook;
	}

	/**
	 * Processes the input and creates a file
	 *
	 * @param xls
	 * @return
	 * @throws IOException
	 */
	public final byte[] process(boolean xls) {
		try {
			// retrieve all store series based on the IDs
			List<ID> ids = service.findIds(getFilter(), sortOrders);
			DataSetIterator<ID, T> iterator = new DataSetIterator<ID, T>(ids, getPageSize()) {

				@Override
				protected List<T> readPage(List<ID> ids) {
					return service.fetchByIds(ids, new SortOrders(sortOrders), joins);
				}
			};

			if (xls) {
				return generateXls(iterator);
			}
			return generateCsv(iterator);
		} catch (IOException ex) {
			throw new OCSRuntimeException(ex.getMessage(), ex);
		}
	}

	/**
	 * Resizes all columns on a sheet if possible
	 *
	 * @param sheet
	 *            the sheet
	 */
	protected void resizeColumns(Sheet sheet) {
		if (canResize()) {
			for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}
		}
	}

	public void setGenerator(XlsStyleGenerator<ID, T> generator) {
		this.generator = generator;
	}

	protected void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	protected void writeCellValue(Cell cell, Object value, EntityModel<T> em, AttributeModel am) {
		if (value instanceof Integer || value instanceof Long) {
			// integer or long numbers
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Date && (am == null || !am.isWeek())) {
			cell.setCellValue((Date) value);
		} else if (value instanceof LocalDate) {
			cell.setCellValue(DateUtils.toLegacyDate((LocalDate) value));
		} else if (value instanceof LocalDateTime) {
			cell.setCellValue(DateUtils.toLegacyDate((LocalDateTime) value));
		} else if (value instanceof BigDecimal) {
			boolean isPercentage = am != null && am.isPercentage();
			if (isPercentage) {
				// percentages in the application are just numbers,
				// but in Excel they are fractions that
				// are displayed as percentages -> so, divide by 100
				double temp = ((BigDecimal) value)
						.divide(MathUtils.HUNDRED, DynamoConstants.INTERMEDIATE_PRECISION, RoundingMode.HALF_UP)
						.setScale(am.getPrecision() + 2, RoundingMode.HALF_UP).doubleValue();
				cell.setCellValue(temp);
			} else {
				cell.setCellValue(((BigDecimal) value)
						.setScale(am == null ? 2 : am.getPrecision(), RoundingMode.HALF_UP).doubleValue());
			}
		} else if (am != null) {
			// use the attribute model
			String str = FormatUtils.formatPropertyValue(entityModelFactory, am, value, ", ");
			cell.setCellValue(str);
		}
	}
}
