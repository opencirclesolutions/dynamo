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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.table.ModelBasedTreeTable;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.ocs.dynamo.utils.StringUtils;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.addon.tableexport.TableExport;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Action Handler which adds export functionality to a table.
 * 
 * @author Patrick Deenen
 *
 */
public class TableExportActionHandler implements Handler {

	/**
	 * A model-based export to CSV
	 * 
	 * @author bas.rutten
	 *
	 */
	class ModelCSVExport extends TableExport implements HasReportTitle {

		private static final long serialVersionUID = -1932835869230658150L;

		private CSVWriter writer;

		private ByteArrayOutputStream out;

		private String exportFileName;

		private String reportTitle;

		private Container container;

		/**
		 * Constructor
		 * 
		 * @param table
		 *            the table to export the data from
		 */
		ModelCSVExport(Table table) {
			super(table);
		}

		/**
		 * Adds a single data row
		 * 
		 * @param rootItemId
		 *            the ID of the item at the root of the row
		 */
		protected void addDataRow(Object rootItemId) {
			List<Object> props = getPropIds();

			Property<?> prop;
			Object propId;
			Object value;

			Item item = container.getItem(rootItemId);
			List<String> fields = new ArrayList<>();
			for (Object prop1 : props) {
				propId = prop1;
				prop = getProperty(item, rootItemId, propId);
				value = prop == null ? null : prop.getValue();
				if (value != null) {
					AttributeModel am = findAttributeModel(propId);
					if (am != null) {
						value = FormatUtils.formatPropertyValue(entityModelFactory, am, value, ", ");
					}
					if (value instanceof String) {
						value = StringUtils.replaceHtmlBreaks(value.toString());
					} else {
						// apply default formatting for numbers
						value = NumberUtils.format(value);
					}
				}
				fields.add(value == null ? "" : value.toString());
			}
			writer.writeNext(fields.toArray(new String[0]));
		}

		/**
		 * Adds a row and all its child rows
		 * 
		 * @param rootItemId
		 *            the ID of the parent/root item
		 */
		protected void addDataRowRecursively(Object rootItemId) {
			addDataRow(rootItemId);
			if (container != null && ((Container.Hierarchical) container).hasChildren(rootItemId)) {
				Collection<?> children = ((Container.Hierarchical) getTableHolder().getContainerDataSource())
						.getChildren(rootItemId);
				children.forEach(c -> addDataRowRecursively(c));
			}
		}

		/**
		 * Iterates over the data rows and adds them to the export
		 */
		protected void addDataRows() {
			container.getItemIds().forEach(i -> addDataRow(i));
		}

		/**
		 * Add the header row
		 */
		protected void addHeaderRow() {
			Object propId;
			List<String> headers = new ArrayList<>();
			for (int col = 0; col < getPropIds().size(); col++) {
				propId = getPropIds().get(col);
				String value = getTableHolder().getColumnHeader(propId);
				headers.add(value == null ? "" : value.trim());
			}
			writer.writeNext(headers.toArray(new String[0]));
		}

		/**
		 * Adds a hierarchy of rows
		 */
		protected void addHierarchicalDataRows() {
			final Collection<?> roots;
			roots = ((Container.Hierarchical) getTableHolder().getContainerDataSource()).rootItemIds();
			for (Object rootId : roots) {
				addDataRowRecursively(rootId);
			}
		}

		@Override
		public void convertTable() {
			out = new ByteArrayOutputStream();
			writer = new CSVWriter(new OutputStreamWriter(out), SystemPropertyUtils.getExportCsvSeparator().charAt(0),
					SystemPropertyUtils.getExportCsvQuoteChar().charAt(0));
			container = getTableHolder().getContainerDataSource();
			addHeaderRow();
			if (isHierarchical()) {
				addHierarchicalDataRows();
			} else {
				addDataRows();
			}
		}

		public String getExportFileName() {
			return exportFileName;
		}

		/**
		 * Overruled from parent class to support model based formatting. Method is
		 * private in parent.
		 * 
		 * @param item
		 *            the item to render
		 * @param rootItemId
		 *            the ID of the item
		 * @param propId
		 *            the ID of the property
		 */
		protected Property<?> getProperty(Item item, Object rootItemId, Object propId) {
			Property<?> prop;
			if (getTableHolder().isGeneratedColumn(propId)) {
				prop = getTableHolder().getPropertyForGeneratedColumn(propId, rootItemId);
			} else {
				prop = item.getItemProperty(propId);
			}
			return prop;
		}

		/**
		 * Returns the property IDs that are to be included in the sheet. Overwritten so
		 * that grid properties are displayed in the correct order
		 */
		@Override
		public List<Object> getPropIds() {
			if (columnIds != null) {
				return columnIds.stream().collect(Collectors.toList());
			}
			return super.getPropIds();
		}

		@Override
		public String getReportTitle() {
			return reportTitle;
		}

		@Override
		public boolean sendConverted() {
			File tempFile = null;
			try {
				writer.flush();
				tempFile = File.createTempFile("tmp", ".csv");
				FileUtils.writeByteArrayToFile(tempFile, out.toByteArray());
				setMimeType(CSV_MIME_TYPE);
				UI tableUI = getTableHolder().getUI();
				return super.sendConvertedFileToUser(tableUI != null ? tableUI : ui, tempFile, exportFileName);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				return false;
			} finally {
				if (tempFile != null) {
					tempFile.delete();
				}
				try {
					writer.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		public void setExportFileName(String exportFileName) {
			this.exportFileName = exportFileName;
		}

		public void setReportTitle(String reportTitle) {
			this.reportTitle = reportTitle;
		}
	}

	/**
	 * A model-based excel Export
	 * 
	 * @author bas.rutten
	 *
	 */
	class ModelExcelExport extends ExcelExport implements HasReportTitle {

		private static final long serialVersionUID = 5811530790417796915L;

		private CellStyle bigDecimalPercentageStyle;

		private CellStyle bigDecimalStyle;

		/**
		 * Style for (integer) numbers cells
		 */
		private CellStyle numberStyle;

		/**
		 * Style for (percentage) integer numbers cells
		 */
		private CellStyle numberPercentageStyle;

		/**
		 * Style for normal (text) cells
		 */
		private CellStyle normal;

		/**
		 * Map to keep track of which total rows contain percentages
		 */
		private Map<Integer, Boolean> percentages = new HashMap<>();

		/**
		 * Constructor
		 * 
		 * @param table
		 */
		ModelExcelExport(Table table, Workbook workBook) {
			super(table, workBook, messageService.getMessage("ocs.export", VaadinUtils.getLocale()),
					TableExportActionHandler.this.reportTitle, null, TableExportActionHandler.this.totalsRow);

			DataFormat format = workbook.createDataFormat();
			numberStyle = workbook.createCellStyle();
			numberStyle.setAlignment(CellStyle.ALIGN_RIGHT);
			addBorder(numberStyle);
			numberStyle.setDataFormat(format.getFormat("#,#"));

			numberPercentageStyle = workbook.createCellStyle();
			numberPercentageStyle.setAlignment(CellStyle.ALIGN_RIGHT);
			addBorder(numberPercentageStyle);
			numberPercentageStyle.setDataFormat(format.getFormat("#,#%"));

			bigDecimalStyle = workbook.createCellStyle();
			bigDecimalStyle.setAlignment(CellStyle.ALIGN_RIGHT);
			addBorder(bigDecimalStyle);
			bigDecimalStyle.setDataFormat(format.getFormat("#,##0.00##"));

			bigDecimalPercentageStyle = workbook.createCellStyle();
			bigDecimalPercentageStyle.setAlignment(CellStyle.ALIGN_RIGHT);
			addBorder(bigDecimalPercentageStyle);
			bigDecimalPercentageStyle.setDataFormat(format.getFormat("#,##0.00%"));

			normal = workbook.createCellStyle();
			normal.setAlignment(CellStyle.ALIGN_LEFT);
			addBorder(normal);
		}

		/**
		 * Overruled from parent class to support model based formatting.
		 * 
		 * @param sheetToAddTo
		 *            the sheet
		 * @param rootItemId
		 *            the ID of the item
		 * @param row
		 *            the index of the current row
		 */
		@Override
		protected void addDataRow(Sheet sheetToAddTo, Object rootItemId, int row) {

			final Row sheetRow = sheetToAddTo.createRow(row);
			Property<?> prop;
			Object propId;
			Object value;
			Cell sheetCell;

			List<Object> props = getPropIds();
			Item item = getTableHolder().getContainerDataSource().getItem(rootItemId);
			for (int col = 0; col < props.size(); col++) {

				// retrieve the property (be sure to get it directly from the
				// item for a huge performance gain)
				propId = props.get(col);
				prop = getProperty(item, rootItemId, propId);
				value = prop == null ? null : prop.getValue();
				sheetCell = sheetRow.createCell(col);

				CellStyle custom = null;
				CellStyle standard = normal;

				if (value != null) {
					AttributeModel am = findAttributeModel(propId);
					boolean isPercentage = am != null && am.isPercentage();

					// custom formatting for certain cells
					if (cellStyleGenerator != null) {
						custom = cellStyleGenerator.getCustomCellStyle(workbook, item, rootItemId, propId, value, am);
					}

					// for numbers we do not use the default formatting since
					// that would produce strings and we
					// want actual numerical values!
					if (NumberUtils.isLong(value) || NumberUtils.isInteger(value)) {
						if (isPercentage) {
							sheetCell.setCellValue(toPercentageValue(am, value));
							standard = numberPercentageStyle;
							percentages.put(col, true);
						} else {
							standard = numberStyle;
							sheetCell.setCellValue(((Number) value).doubleValue());
							percentages.put(col, false);
						}
					} else if (value instanceof BigDecimal) {
						if (isPercentage) {
							sheetCell.setCellValue(toPercentageValue(am, value));
							standard = bigDecimalPercentageStyle;
							percentages.put(col, true);
						} else {
							// just display as a number
							sheetCell.setCellValue(
									((BigDecimal) value).setScale(SCALE, RoundingMode.HALF_UP).doubleValue());
							standard = bigDecimalStyle;
							percentages.put(col, false);
						}
					} else if (am != null) {
						// if it's an actual model attribute, then defer to the
						// normal formatting functionality

						// replaces all HTML line breaks
						if (value instanceof String) {
							value = StringUtils.replaceHtmlBreaks((String) value);
						}
						sheetCell.setCellValue(FormatUtils.formatPropertyValue(entityModelFactory, am, value, ", "));
					} else {
						// if everything else fails, use the string
						// representation
						value = StringUtils.replaceHtmlBreaks(value.toString());
						sheetCell.setCellValue(value.toString());
					}
				}

				if (custom != null) {
					sheetCell.setCellStyle(custom);
				} else if (standard != null) {
					sheetCell.setCellStyle(standard);
				}
			}
		}

		/**
		 * Adds a border to a cell style
		 * 
		 * @param style
		 *            the cell style
		 */
		private void addBorder(CellStyle style) {
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setBorderRight(CellStyle.BORDER_THIN);
		}

		/**
		 * Add row recursively - overwritten to provide true unlimited recursion, rather
		 * than just one level
		 * 
		 * @param sheetToAddTo
		 *            the sheet
		 * @param rootItemId
		 *            the ID of the root item
		 * @param row
		 *            the index of the current row
		 * @return
		 */
		private int addDataRowRecursively(final Sheet sheetToAddTo, final Object rootItemId, final int row) {
			int numberAdded = 0;
			int localRow = row;

			// add the row itself
			addDataRow(sheetToAddTo, rootItemId, row);
			numberAdded++;

			// iterate over the children
			if (((Container.Hierarchical) getTableHolder().getContainerDataSource()).hasChildren(rootItemId)) {
				final Collection<?> children = ((Container.Hierarchical) getTableHolder().getContainerDataSource())
						.getChildren(rootItemId);

				int childCount = 0;
				for (final Object child : children) {

					// process recursively
					childCount = addDataRowRecursively(sheetToAddTo, child, localRow + 1);

					// increment the row counter by the number of added rows
					// (remember we already incremented it by one)
					localRow += childCount;
					numberAdded += childCount;
				}

				if (displayTotals) {
					addDataRow(hierarchicalTotalsSheet, rootItemId, localRow);
				}

				// apply grouping (when not streaming)
				if (numberAdded > 1 && !isStreaming()) {
					sheet.groupRow(row + 1, (row + numberAdded) - 1);
					sheet.setRowGroupCollapsed(row + 1, true);
				}
			}
			return numberAdded;
		}

		/**
		 * Adds a hierarchical data row
		 * 
		 * @param sheetToAddTo
		 *            the sheet to add the row to
		 * @param row
		 *            the row
		 */
		@Override
		protected int addHierarchicalDataRows(final Sheet sheetToAddTo, final int row) {
			final Collection<?> roots;
			int localRow = row;
			roots = ((Container.Hierarchical) getTableHolder().getContainerDataSource()).rootItemIds();
			/*
			 * For Hierarchical Containers, the outlining/grouping in the sheet is with the
			 * summary row at the top and the grouped/outlined subcategories below.
			 */
			sheet.setRowSumsBelow(false);
			int count = 0;
			for (final Object rootId : roots) {
				count = addDataRowRecursively(sheetToAddTo, rootId, localRow);
				localRow = localRow + count;
			}
			return localRow;
		}

		/**
		 * Adds a total rows to the bottom of the sheet - overwritten so that the
		 * columns can be property aligned
		 */
		@Override
		protected void addTotalsRow(final int currentRow, final int startRow) {
			if (!isStreaming()) {
				totalsRow = sheet.createRow(currentRow);
				totalsRow.setHeightInPoints(30);
				Cell cell;
				CellRangeAddress cra;
				for (int col = 0; col < getPropIds().size(); col++) {
					final Object propId = getPropIds().get(col);
					cell = totalsRow.createCell(col);
					cell.setCellStyle(getCellStyle(currentRow, startRow, col, true));
					final Short poiAlignment = getTableHolder().getCellAlignment(propId);
					CellUtil.setAlignment(cell, workbook, poiAlignment);
					final Class<?> propType = getPropertyType(propId);
					if (NumberUtils.isNumeric(propType)) {
						boolean perc = Boolean.TRUE.equals(percentages.get(col));
						if (NumberUtils.isInteger(propType) || NumberUtils.isLong(propType)) {
							cell.setCellStyle(perc ? numberPercentageStyle : numberStyle);
						} else if (BigDecimal.class.equals(propType)) {
							cell.setCellStyle(perc ? bigDecimalPercentageStyle : bigDecimalStyle);
						}
						cra = new CellRangeAddress(startRow, currentRow - 1, col, col);
						if (isHierarchical()) {
							// 9 & 109 are for sum. 9 means include hidden
							// cells,
							// 109 means exclude.
							// this will show the wrong value if the user
							// expands an
							// outlined category, so
							// we will range value it first
							cell.setCellFormula(
									"SUM(" + cra.formatAsString(hierarchicalTotalsSheet.getSheetName(), true) + ")");
						} else {
							cell.setCellFormula("SUM(" + cra.formatAsString() + ")");
						}
					} else {
						if (col == 0) {
							cell.setCellValue(createHelper.createRichTextString(
									messageService.getMessage("ocs.total", VaadinUtils.getLocale())));
						}
					}
				}
			}
		}

		/**
		 * Converts a value to a percentage (basically divide it by 100)
		 * 
		 * @param am
		 *            the attribute model for the value to converter
		 * @param value
		 *            the value to convert - must be a number
		 * @return
		 */
		private double toPercentageValue(AttributeModel am, Object value) {
			BigDecimal bd = null;
			if (value instanceof BigDecimal) {
				bd = (BigDecimal) value;
			} else {
				bd = BigDecimal.valueOf(((Number) value).doubleValue());
			}
			return bd.divide(HUNDRED, DynamoConstants.INTERMEDIATE_PRECISION, RoundingMode.HALF_UP)
					.setScale(am.getPrecision() + 2, RoundingMode.HALF_UP).doubleValue();
		}

		/**
		 * Final sheet formatting - this is skipped for streaming workbooks since they
		 * cannot perform actions on all the rows any more
		 */
		@Override
		protected void finalSheetFormat() {
			if (!isStreaming()) {
				super.finalSheetFormat();
			}
		}

		/**
		 * Overruled from parent class to support model based formatting. Method is
		 * private in parent.
		 */
		protected Property<?> getProperty(Item item, Object rootItemId, Object propId) {
			Property<?> prop;
			if (getTableHolder().isGeneratedColumn(propId)) {
				prop = getTableHolder().getPropertyForGeneratedColumn(propId, rootItemId);
			} else {
				prop = item.getItemProperty(propId);
			}
			return prop;
		}

		/**
		 * Reintroduced method because it is private in the parent class - retrieve the
		 * type of a property
		 * 
		 * @param propId
		 *            the property
		 * @return
		 */
		private Class<?> getPropertyType(final Object propId) {
			Class<?> classType;
			if (getTableHolder().isGeneratedColumn(propId)) {
				classType = getTableHolder().getPropertyTypeForGeneratedColumn(propId);
			} else {
				classType = getTableHolder().getContainerDataSource().getType(propId);
			}
			return classType;
		}

		/**
		 * Returns the property IDs that are to be included in the sheet. Overwritten so
		 * that grid properties are displayed in the correct order
		 */
		@Override
		public List<Object> getPropIds() {
			if (columnIds != null) {
				return columnIds.stream().collect(Collectors.toList());
			}
			return super.getPropIds();
		}

		private boolean isStreaming() {
			return workbook instanceof SXSSFWorkbook;
		}

		/**
		 * Overwritten so we can use the correct mime type and make sure the UI is not
		 * null
		 */
		@Override
		public boolean sendConverted() {
			File tempFile = null;
			try {
				tempFile = File.createTempFile("tmp", ".xlsx");
				FileOutputStream fileOut = new FileOutputStream(tempFile);
				workbook.write(fileOut);
				setMimeType(MIME_TYPE_XLSX);
				UI tableUI = getTableHolder().getUI();
				return super.sendConvertedFileToUser(tableUI != null ? tableUI : ui, tempFile, exportFileName);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				return false;
			} finally {
				if (workbook != null) {
					try {
						workbook.close();
					} catch (IOException ex) {
						// do nothing
					}
				}

				if (tempFile != null) {
					tempFile.delete();
				}
			}
		}
	}

	private static final Logger LOG = Logger.getLogger(TableExportActionHandler.class);

	private static final BigDecimal HUNDRED = new BigDecimal(100);

	private static final String MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private static final int SCALE = 5;

	private static final long serialVersionUID = -276477801970861419L;

	private Action actionExport;

	private List<String> columnIds;

	private EntityModelFactory entityModelFactory;

	private List<EntityModel<?>> entityModels;

	private MessageService messageService;

	private String reportTitle;

	private boolean totalsRow;

	/**
	 * The maximum amount of rows that is allowed streaming
	 */
	private int maxRowsStreaming;

	/**
	 * The maximum amount of rows that is allowed when not streaming
	 */
	private int maxRowsNonStreaming;

	private UI ui;

	private CustomCellStyleGenerator cellStyleGenerator;

	private TableExportMode exportMode;

	/**
	 * Constructor
	 * 
	 * @param ui
	 *            the current UI
	 * @param entityModels
	 *            the entity models of the entities used in the export
	 * @param reportTitle
	 *            the title of the report
	 * @param columnIds
	 *            the IDs of the columns
	 * @param totalsRow
	 *            whether to include a totals row
	 * @param exportMode
	 *            the export mode
	 * @param cellStyleGenerator
	 *            the cell style generator for custom styling
	 */
	public TableExportActionHandler(UI ui, List<EntityModel<?>> entityModels, String reportTitle,
			List<String> columnIds, boolean totalsRow, TableExportMode exportMode,
			CustomCellStyleGenerator cellStyleGenerator) {
		this(ui, columnIds, reportTitle, totalsRow, exportMode, cellStyleGenerator);
		this.entityModels = entityModels;
	}

	/**
	 * Constructor
	 * 
	 * @param ui
	 *            the current UI
	 * @param reportTitle
	 *            the title of the report
	 * @param columnIds
	 *            the IDs of the columns
	 * @param totalsRow
	 *            whether to include a totals row
	 * @param exportMode
	 *            the export mode
	 * @param cellStyleGenerator
	 *            the cell style generator for custom styling
	 */
	public TableExportActionHandler(UI ui, List<String> columnIds, String reportTitle, boolean totalsRow,
			TableExportMode exportMode, CustomCellStyleGenerator cellStyleGenerator) {
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();
		this.columnIds = columnIds;
		this.cellStyleGenerator = cellStyleGenerator;
		this.exportMode = exportMode;
		this.maxRowsNonStreaming = SystemPropertyUtils.getMaximumExportRowsNonStreaming();
		this.maxRowsStreaming = SystemPropertyUtils.getMaximumExportRowsStreaming();

		// try to get message from message bundle
		this.reportTitle = messageService.getMessageNoDefault(reportTitle, VaadinUtils.getLocale());
		if (this.reportTitle == null) {
			// if that fails, use the title itself
			this.reportTitle = reportTitle;
		}
		this.ui = ui;
		this.totalsRow = totalsRow;
		actionExport = new Action(TableExportMode.CSV.equals(exportMode)
				? messageService.getMessage("ocs.export.csv", VaadinUtils.getLocale())
				: (TableExportMode.EXCEL_SIMPLIFIED.equals(exportMode)
						? messageService.getMessage("ocs.export.simple", VaadinUtils.getLocale())
						: messageService.getMessage("ocs.export", VaadinUtils.getLocale())));
	}

	/**
	 * Creates the appropriate workbook based on the size of the data set
	 * 
	 * @param size
	 *            the size of the data set (in rows)
	 * 
	 * @return
	 */
	protected Workbook createWorkbook(int size) {
		if (!TableExportMode.EXCEL_SIMPLIFIED.equals(exportMode) && size < maxRowsNonStreaming) {
			return new XSSFWorkbook();
		} else if (size < maxRowsStreaming) {
			// simple mode, or too large for fancy workbook
			return new SXSSFWorkbook();
		}
		return null;
	}

	/**
	 * Export a grid - this is achieved by wrapping the data source from the grid in
	 * a table
	 * 
	 * @param grid
	 *            the grid to export from
	 */
	public void exportFromGrid(Grid grid) {
		exportFromGrid(grid, null);
	}

	public void exportFromGrid(Grid grid, Object target) {
		Table table = new Table();
		table.setContainerDataSource(grid.getContainerDataSource());

		// copy header captions from grid to table
		for (Column c : grid.getColumns()) {
			Object oid = c.getPropertyId();

			List<String> caption = new ArrayList<>();
			for (int i = 0; i < grid.getHeaderRowCount(); i++) {
				HeaderRow r = grid.getHeaderRow(i);
				try {
					caption.add(r.getCell(oid).getText());
				} catch (Exception ex) {
					// if it is not text, then it is HTML (very ugly, but seems
					// to be the only way)
					caption.add(r.getCell(oid).getHtml());
				}
			}
			table.setColumnHeader(c.getPropertyId(),
					caption.stream().collect(Collectors.joining(" ")).replaceAll("<\\w+/>", " "));
		}
		handleAction(actionExport, table, null);
	}

	/**
	 * Looks up an attribute model in the list of available models
	 * 
	 * @param propId
	 *            the ID of the property for which to find the attribute model
	 * @return
	 */
	protected AttributeModel findAttributeModel(Object propId) {
		if (entityModels != null) {
			String prop = propId.toString();
			int p = prop.indexOf('_');
			String pString = p >= 0 ? prop.substring(p + 1) : prop;
			return entityModels.stream().filter(em -> em.getAttributeModel(pString) != null)
					.map(em -> em.getAttributeModel(pString)).findFirst().orElse(null);
		}
		return null;
	}

	/**
	 * 
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		return new Action[] { actionExport };
	}

	/**
	 * 
	 * @return a formatted date
	 */
	private String getFormattedDate() {
		return DateUtils.formatDateTime(LocalDateTime.now(), SystemPropertyUtils.getDefaultDateTimeFormat());
	}

	/**
	 * Handles the action
	 * 
	 * @param action
	 *            the action
	 * @param sender
	 *            the component that sent the request (the table or the grid)
	 * @param target
	 *            the target of the action - not used in this case
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == actionExport && sender != null && sender instanceof Table) {
			Table table = (Table) sender;

			// determine the size of the data set
			int size = 0;

			if (sender instanceof TreeTable) {
				// for a tree table, look at the size on the lowest level
				TreeTable tt = (TreeTable) sender;
				if (tt.getContainerDataSource() instanceof ModelBasedHierarchicalContainer) {
					ModelBasedHierarchicalContainer<?> hc = (ModelBasedHierarchicalContainer<?>) tt
							.getContainerDataSource();
					size = hc.getBottomLevelSize();
				} else {
					size = table.getContainerDataSource().size();
				}
			} else if (sender instanceof Table) {
				size = table.getContainerDataSource().size();
			}

			TableExportService service = ServiceLocatorFactory.getServiceLocator().getService(TableExportService.class);

			if (TableExportMode.CSV.equals(exportMode)) {
				ModelCSVExport export = new ModelCSVExport(table);
				if (table instanceof TreeTable) {
					export.getTableHolder().setHierarchical(true);
				}
				export.setExportFileName((reportTitle + " " + getFormattedDate() + ".csv").replace(' ', '_'));
				export.setReportTitle(reportTitle);
				service.export(export);
			} else {

				// create the workbook
				Workbook wb = createWorkbook(size);
				if (wb == null) {
					// if there are too many rows, display a message
					Notification.show(messageService.getMessage("ocs.export.too.large", VaadinUtils.getLocale()),
							Notification.Type.ERROR_MESSAGE);
				} else {
					ExcelExport export = new ModelExcelExport(table, wb);
					export.setReportTitle(reportTitle);
					export.setRowHeaders(false);
					if (table instanceof TreeTable) {
						export.getTableHolder().setHierarchical(true);
						// a model based tree table has multiple levels - the
						// built-in
						// totals calculation doesn't work in that case
						export.setDisplayTotals(!(sender instanceof ModelBasedTreeTable));
					}
					export.setExportFileName((reportTitle + " " + getFormattedDate() + ".xlsx").replace(' ', '_'));
					service.export(export);
				}
			}

		} else if (action == actionExport && sender != null && sender instanceof Grid) {
			exportFromGrid((Grid) sender, target);
		}
	}
}
