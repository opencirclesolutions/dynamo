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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junitx.util.PrivateAccessor;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.importer.impl.BaseXlsImporter;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;
import com.ocs.dynamo.ui.composite.table.InMemoryTreeTable;
import com.ocs.dynamo.ui.composite.table.Department;
import com.ocs.dynamo.ui.composite.table.ModelBasedTable;
import com.ocs.dynamo.ui.composite.table.Person;
import com.vaadin.addon.tableexport.TemporaryFileDownloadResource;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;

public class TableExportActionHandlerTest extends BaseMockitoTest {

	private TableExportActionHandler handler;

	private static final String REPORT_TITLE = "Report Title";

	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	private List<String> columnIds = Lists.newArrayList("name", "age", "weight", "percentage");

	private List<String> shortColumnIds = Lists.newArrayList("name", "age");

	@Mock
	private MessageService messageService;

	private BaseXlsImporter importer = new BaseXlsImporter();

	@Mock
	private UI ui;

	@Mock
	private Page page;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		MockUtil.mockMessageService(messageService);
		Mockito.when(ui.getPage()).thenReturn(page);
		PrivateAccessor.setField(entityModelFactory, "messageService", messageService);

		// set the default values for the system properties
		System.setProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ";");
		System.setProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "\"");
	}

	@Test
	public void testExportSimpleWithoutEntityModelExcel() throws IOException {

		handler = new TableExportActionHandler(ui, columnIds, REPORT_TITLE, false, TableExportMode.EXCEL, null);

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);

		byte[] bytes = captureSave();
		Workbook wb = importer.createWorkbook(bytes);

		// string
		Assert.assertEquals("Bas, Bob", wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals("Patrick", wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());

		// integer
		Assert.assertEquals(35, wb.getSheetAt(0).getRow(2).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(44, wb.getSheetAt(0).getRow(3).getCell(1).getNumericCellValue(), 0.001);

		// bigdecimal
		Assert.assertEquals(76.0, wb.getSheetAt(0).getRow(2).getCell(2).getNumericCellValue(), 0.001);
		Assert.assertEquals(77.0, wb.getSheetAt(0).getRow(3).getCell(2).getNumericCellValue(), 0.001);

		// without an entity model, the application doesn't know this is a
		// percentage
		Assert.assertEquals(12, wb.getSheetAt(0).getRow(2).getCell(3).getNumericCellValue(), 0.001);
		Assert.assertEquals(15, wb.getSheetAt(0).getRow(3).getCell(3).getNumericCellValue(), 0.001);
	}

	@Test
	public void testExportSimpleWithoutEntityModelCsv() throws IOException {

		handler = new TableExportActionHandler(ui, columnIds, REPORT_TITLE, false, TableExportMode.CSV, null);

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);

		byte[] bytes = captureSave();
		List<String> lines = IOUtils.readLines(new ByteArrayInputStream(bytes));

		Assert.assertEquals("\"Name\";\"Age\";\"Weight\";\"Percentage\"", lines.get(0));
		Assert.assertEquals("\"Bas, Bob\";\"35\";\"" + formatNumber("76,00") + "\";\"" + formatNumber("12,00") + "\"",
		        lines.get(1));
		Assert.assertEquals("\"Patrick\";\"44\";\"" + formatNumber("77,00") + "\";\"" + formatNumber("15,00") + "\"",
		        lines.get(2));
	}

	@Test
	public void testExportSimpleWithEntityModelCsv() throws IOException {

		List<EntityModel<?>> models = new ArrayList<>();
		models.add(entityModelFactory.getModel(Person.class));

		handler = new TableExportActionHandler(ui, models, REPORT_TITLE, columnIds, false, TableExportMode.CSV, null);

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);
		byte[] bytes = captureSave();
		List<String> lines = IOUtils.readLines(new ByteArrayInputStream(bytes));

		Assert.assertEquals("\"Name\";\"Age\";\"Weight\";\"Percentage\"", lines.get(0));
		Assert.assertEquals("\"Bas, Bob\";\"35\";\"" + "76,00" + "\";\"" + "12,00" + "%\"", lines.get(1));
		Assert.assertEquals("\"Patrick\";\"44\";\"" + "77,00" + "\";\"" + "15,00" + "%\"", lines.get(2));
	}

	@Test
	public void testExportTreeTableExcel() throws IOException {

		handler = new TableExportActionHandler(ui, shortColumnIds, REPORT_TITLE, false, TableExportMode.EXCEL, null);
		handler.handleAction(handler.getActions(null, null)[0], getTreeTable(), null);

		byte[] bytes = captureSave();
		Workbook wb = importer.createWorkbook(bytes);

		// string
		Assert.assertEquals("Special ops", wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals("Bas, Bob", wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());
		Assert.assertEquals("Patrick", wb.getSheetAt(0).getRow(4).getCell(0).getStringCellValue());

		// integer
		Assert.assertEquals(35, wb.getSheetAt(0).getRow(3).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(44, wb.getSheetAt(0).getRow(4).getCell(1).getNumericCellValue(), 0.001);
	}

	@Test
	public void testExportTreeTableCsv() throws IOException {

		handler = new TableExportActionHandler(ui, shortColumnIds, REPORT_TITLE, false, TableExportMode.CSV, null);
		handler.handleAction(handler.getActions(null, null)[0], getTreeTable(), null);

		byte[] bytes = captureSave();
		List<String> lines = IOUtils.readLines(new ByteArrayInputStream(bytes));

		Assert.assertEquals("\"name\";\"age\"", lines.get(0));
		Assert.assertEquals("\"Special ops\";\"\"", lines.get(1));
		Assert.assertEquals("\"Bas, Bob\";\"35\"", lines.get(2));
		Assert.assertEquals("\"Patrick\";\"44\"", lines.get(3));
	}

	@Test
	public void testExportGrid() throws IOException {

		handler = new TableExportActionHandler(ui, columnIds, REPORT_TITLE, false, TableExportMode.EXCEL, null);

		handler.exportFromGrid(getGrid());

		byte[] bytes = captureSave();
		Workbook wb = importer.createWorkbook(bytes);

		// headers
		Assert.assertEquals("Naam", wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
		Assert.assertEquals("Leeftijd", wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue());
		Assert.assertEquals("Gewicht", wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue());
		Assert.assertEquals("Percentage", wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue());

		// string
		Assert.assertEquals("Bas, Bob", wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals("Patrick", wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());

		// integer
		Assert.assertEquals(35, wb.getSheetAt(0).getRow(2).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(44, wb.getSheetAt(0).getRow(3).getCell(1).getNumericCellValue(), 0.001);

		// bigdecimal
		Assert.assertEquals(76.0, wb.getSheetAt(0).getRow(2).getCell(2).getNumericCellValue(), 0.001);
		Assert.assertEquals(77.0, wb.getSheetAt(0).getRow(3).getCell(2).getNumericCellValue(), 0.001);

		// without an entity model, the application doesn't know this is a
		// percentage
		Assert.assertEquals(12, wb.getSheetAt(0).getRow(2).getCell(3).getNumericCellValue(), 0.001);
		Assert.assertEquals(15, wb.getSheetAt(0).getRow(3).getCell(3).getNumericCellValue(), 0.001);
	}

	@Test
	public void testExportSimpleWithEntityModel() throws IOException {

		List<EntityModel<?>> models = new ArrayList<>();
		models.add(entityModelFactory.getModel(Person.class));

		handler = new TableExportActionHandler(ui, models, REPORT_TITLE, columnIds, false, TableExportMode.EXCEL, null);

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);

		byte[] bytes = captureSave();
		Workbook wb = importer.createWorkbook(bytes);

		// string
		Assert.assertEquals("Bas, Bob", wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals("Patrick", wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());

		// integer
		Assert.assertEquals(35, wb.getSheetAt(0).getRow(2).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(44, wb.getSheetAt(0).getRow(3).getCell(1).getNumericCellValue(), 0.001);

		// bigdecimal
		Assert.assertEquals(76.0, wb.getSheetAt(0).getRow(2).getCell(2).getNumericCellValue(), 0.001);
		Assert.assertEquals(77.0, wb.getSheetAt(0).getRow(3).getCell(2).getNumericCellValue(), 0.001);

		// percentage
		Assert.assertEquals(0.12, wb.getSheetAt(0).getRow(2).getCell(3).getNumericCellValue(), 0.001);
		Assert.assertEquals(0.15, wb.getSheetAt(0).getRow(3).getCell(3).getNumericCellValue(), 0.001);
	}

	@Test
	public void testExportWithTotalsRow() throws IOException {
		handler = new TableExportActionHandler(ui, columnIds, REPORT_TITLE, true, TableExportMode.EXCEL, null);

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);

		byte[] bytes = captureSave();
		Workbook wb = importer.createWorkbook(bytes);

		Assert.assertEquals("Bas, Bob", wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
		Assert.assertEquals("Patrick", wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());

		Assert.assertEquals(35, wb.getSheetAt(0).getRow(2).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(44, wb.getSheetAt(0).getRow(3).getCell(1).getNumericCellValue(), 0.001);

		// totals must be summed up
		Assert.assertEquals(79, wb.getSheetAt(0).getRow(4).getCell(1).getNumericCellValue(), 0.001);

	}

	@Test
	public void testExportWithCustomCellStyle() throws IOException {

		List<EntityModel<?>> models = new ArrayList<>();
		models.add(entityModelFactory.getModel(Person.class));

		handler = new TableExportActionHandler(ui, models, REPORT_TITLE, columnIds, true, TableExportMode.EXCEL,
		        new CustomCellStyleGenerator() {

			        private CellStyle cellStyle;

			        private CellStyle bdStyle;

			        @Override
			        public CellStyle getCustomCellStyle(Workbook workbook, Item item, Object rootItemId, Object propId,
			                Object value, AttributeModel attributeModel) {
				        if (cellStyle == null) {
					        cellStyle = workbook.createCellStyle();
					        cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
					        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
					        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
					        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
					        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
					        Font font = workbook.createFont();
					        font.setColor(IndexedColors.BLUE.getIndex());
					        cellStyle.setFont(font);
				        }

				        if (bdStyle == null) {
					        DataFormat format = workbook.createDataFormat();
					        bdStyle = workbook.createCellStyle();
					        bdStyle.setAlignment(CellStyle.ALIGN_RIGHT);
					        bdStyle.setBorderBottom(CellStyle.BORDER_THIN);
					        bdStyle.setBorderTop(CellStyle.BORDER_THIN);
					        bdStyle.setBorderLeft(CellStyle.BORDER_THIN);
					        bdStyle.setBorderRight(CellStyle.BORDER_THIN);
					        bdStyle.setDataFormat(format.getFormat("#,##0.00"));

					        Font font = workbook.createFont();
					        font.setColor(IndexedColors.BLUE.getIndex());
					        bdStyle.setFont(font);
				        }

				        if ("name".equals(propId)) {
					        return cellStyle;
				        } else if ("percentage".equals(propId)) {
					        return bdStyle;
				        }
				        return null;
			        }
		        });

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);

		byte[] bytes = captureSave();
		Workbook wb = importer.createWorkbook(bytes);

		Assert.assertEquals("Bas, Bob", wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());
		Font font = wb.getFontAt(wb.getSheetAt(0).getRow(2).getCell(0).getCellStyle().getFontIndex());
		Assert.assertEquals(IndexedColors.BLUE.getIndex(), font.getColor());

		Assert.assertEquals("Patrick", wb.getSheetAt(0).getRow(3).getCell(0).getStringCellValue());

		Assert.assertEquals(35, wb.getSheetAt(0).getRow(2).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(44, wb.getSheetAt(0).getRow(3).getCell(1).getNumericCellValue(), 0.001);

		// totals must be summed up
		Assert.assertEquals(79, wb.getSheetAt(0).getRow(4).getCell(1).getNumericCellValue(), 0.001);

		// percentage
		Assert.assertEquals(0.12, wb.getSheetAt(0).getRow(2).getCell(3).getNumericCellValue(), 0.001);
		Assert.assertEquals(0.15, wb.getSheetAt(0).getRow(3).getCell(3).getNumericCellValue(), 0.001);

	}

	/**
	 * Perform a CSV export with a different separator
	 * 
	 * @throws IOException
	 */
	@Test
	public void testExportSimpleWithoutEntityModelCsvOtherSeparator() throws IOException {

		System.setProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ",");
		System.setProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "'");

		handler = new TableExportActionHandler(ui, columnIds, REPORT_TITLE, false, TableExportMode.CSV, null);

		handler.handleAction(handler.getActions(null, null)[0], getTable(), null);

		byte[] bytes = captureSave();
		List<String> lines = IOUtils.readLines(new ByteArrayInputStream(bytes));

		Assert.assertEquals("'Name','Age','Weight','Percentage'", lines.get(0));
		Assert.assertEquals("'Bas, Bob','35','" + formatNumber("76,00") + "','" + formatNumber("12,00") + "'",
		        lines.get(1));
		Assert.assertEquals("'Patrick','44','" + formatNumber("77,00") + "','" + formatNumber("15,00") + "'",
		        lines.get(2));
	}

	private Table getTable() {
		BeanItemContainer<Person> container = new BeanItemContainer<>(Person.class);

		Person person1 = new Person(1, "Bas<br/>Bob", 35, BigDecimal.valueOf(76.0), BigDecimal.valueOf(12));
		Person person2 = new Person(2, "Patrick", 44, BigDecimal.valueOf(77.0), BigDecimal.valueOf(15));
		container.addAll(Lists.newArrayList(person1, person2));

		return new ModelBasedTable<Integer, Person>(container, entityModelFactory.getModel(Person.class), true);
	}

	private TreeTable getTreeTable() {

		final Person person1 = new Person(1, "Bas<br/>Bob", 35, BigDecimal.valueOf(76.0), BigDecimal.valueOf(12));
		final Person person2 = new Person(2, "Patrick", 44, BigDecimal.valueOf(77.0), BigDecimal.valueOf(15));

		final Department department = new Department();
		department.setName("Special ops");
		department.setEmployees(Sets.newHashSet(person1, person2));

		InMemoryTreeTable<Integer, Person, Integer, Department> table = new InMemoryTreeTable<Integer, Person, Integer, Department>() {

			private static final long serialVersionUID = -6428315820101615753L;

			@Override
			protected boolean isRightAligned(String propertyId) {
				return false;
			}

			@Override
			protected boolean isEditable(String propertyId) {
				return false;
			}

			@Override
			protected Number handleChange(String propertyId, String rowId, String parentRowId, String childKey,
			        String parentKey, Object newValue) {
				return 0;
			}

			@Override
			protected String[] getSumColumns() {
				return new String[0];
			}

			@Override
			protected List<Person> getRowCollection(Department dep) {
				return Lists.newArrayList(person1, person2);
			}

			@Override
			protected String getReportTitle() {
				return "Departments";
			}

			@Override
			protected String getPreviousColumnId(String columnId) {
				return null;
			}

			@Override
			protected List<Department> getParentCollection() {
				return Lists.newArrayList(department);
			}

			@Override
			protected String getKeyPropertyId() {
				return "name";
			}

			@Override
			protected String[] getColumnstoUpdate(String propertyId) {
				return new String[0];
			}

			@Override
			protected void fillParentRow(Object[] row, Department entity) {
				// do nothing
				row[0] = entity.getName();

			}

			@Override
			protected void fillChildRow(Object[] row, Person entity, Department parentEntity) {
				row[0] = entity.getName();
				row[1] = entity.getAge();
			}

			@Override
			protected void addContainerProperties() {
				addContainerProperty("name", String.class, null);
				addContainerProperty("age", Integer.class, null);
			}

			@Override
			protected Class<?> getEditablePropertyClass(String propertyId) {
				return Integer.class;
			}
		};
		table.build();
		return table;
	}

	private Grid getGrid() {
		BeanItemContainer<Person> container = new BeanItemContainer<>(Person.class);

		Person person1 = new Person(1, "Bas<br/>Bob", 35, BigDecimal.valueOf(76.0), BigDecimal.valueOf(12));
		Person person2 = new Person(2, "Patrick", 44, BigDecimal.valueOf(77.0), BigDecimal.valueOf(15));
		container.addAll(Lists.newArrayList(person1, person2));

		Grid grid = new Grid(container);
		grid.getColumn("name").setHeaderCaption("Naam");
		grid.getColumn("age").setHeaderCaption("Leeftijd");
		grid.getColumn("weight").setHeaderCaption("Gewicht");
		grid.getColumn("percentage").setHeaderCaption("Percentage");

		return grid;
	}

	@SuppressWarnings({ "deprecation" })
	private byte[] captureSave() throws IOException {

		ArgumentCaptor<TemporaryFileDownloadResource> captor = ArgumentCaptor
		        .forClass(TemporaryFileDownloadResource.class);
		Mockito.verify(page).open(captor.capture(), Matchers.anyString(), Matchers.anyBoolean());
		TemporaryFileDownloadResource resource = captor.getValue();

		// read into a large byte array
		byte[] bytes = new byte[1_000_000];
		int read = IOUtils.read(resource.getStreamSource().getStream(), bytes);

		// copy into an array that actually has the correct size
		byte[] outBytes = new byte[read];
		System.arraycopy(bytes, 0, outBytes, 0, read);

		return outBytes;
	}

}
