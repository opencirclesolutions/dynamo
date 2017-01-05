package com.ocs.dynamo.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.test.BaseMockitoTest;

public class ExportPivotTemplateTest extends BaseMockitoTest {

	@Mock
	private BaseService<Integer, Series> seriesService;

	private List<Week> weeks = new ArrayList<>();

	private List<Series> series = new ArrayList<>();

	private SortOrder[] orders = new SortOrder[] { new SortOrder("name") };

	Filter filter = new Compare.Equal("name", "Kevin");

	@Override
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		super.setUp();

		Week week1 = new Week(1, "Week 1");
		Week week2 = new Week(1, "Week 2");
		Week week3 = new Week(1, "Week 3");
		Week week4 = new Week(1, "Week 4");

		weeks = Lists.newArrayList(week1, week2, week3, week4);

		series.add(new Series(1, 1, BigDecimal.valueOf(1.0), 1, week1));
		series.add(new Series(2, 2, BigDecimal.valueOf(2.0), 1, week2));
		series.add(new Series(3, 3, BigDecimal.valueOf(3.0), 1, week3));
		series.add(new Series(4, 4, BigDecimal.valueOf(4.0), 1, week4));

		series.add(new Series(5, 5, BigDecimal.valueOf(5.0), 2, week1));
		series.add(new Series(6, 6, BigDecimal.valueOf(6.0), 2, week2));
		series.add(new Series(7, 7, BigDecimal.valueOf(7.0), 2, week3));
		series.add(new Series(8, 8, BigDecimal.valueOf(8.0), 2, week4));

		series.add(new Series(9, 9, BigDecimal.valueOf(9.0), 3, week1));
		series.add(new Series(10, 10, BigDecimal.valueOf(10.0), 3, week2));
		series.add(new Series(11, 11, BigDecimal.valueOf(11.0), 3, week3));
		series.add(new Series(12, 12, BigDecimal.valueOf(12.0), 3, week4));

		series.add(new Series(13, 13, BigDecimal.valueOf(13.0), 4, week1));
		series.add(new Series(14, 14, BigDecimal.valueOf(14.0), 4, week2));
		series.add(new Series(15, 15, BigDecimal.valueOf(15.0), 4, week3));
		series.add(new Series(16, 16, BigDecimal.valueOf(16.0), 4, week4));

		List<Integer> ids = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);

		Mockito.when(seriesService.findIds(filter, orders)).thenReturn(ids);
		Mockito.when(seriesService.fetchByIds(Matchers.any(List.class), Matchers.any(SortOrders.class))).thenReturn(
		        series);
	}

	@Test
	public void testSum() throws IOException {

		ExportPivotTemplate<Integer, Series, Integer, Week> template = new ExportPivotTemplate<Integer, Series, Integer, Week>(
		        seriesService, orders, filter, null, Lists.newArrayList("Name"), "Title", true, null) {

			@Override
			protected void setXlsCellValues(Row row, Series entity) {
				createCell(row, 0, entity, null, null).setCellValue(entity.getUnit());
			}

			@Override
			protected void setCsvCellValues(List<String> row, Series entity) {
				row.add(Integer.toString(entity.getUnit()));
			}

			@Override
			protected Object getValue(Series entity) {
				return entity.getValue();
			}

			@Override
			protected String getRowTotalCaption() {
				return "Row total";
			}

			@Override
			protected Object getRowId(Series entity) {
				return entity.getUnit();
			}

			@Override
			protected List<Week> getColumns() {
				return weeks;
			}

			@Override
			protected String getColumnHeader(Week u) {
				return u.getName();
			}

			@Override
			protected boolean createSumColumn() {
				return true;
			}

			@Override
			protected boolean createAveragesColumn() {
				return false;
			}

			@Override
			public int getPageSize() {
				return 1000;
			}

			@Override
			protected boolean columnValueMatches(Series entity, Week column) {
				return true;
			}
		};
		byte[] bytes = template.process(true);
		Assert.assertTrue(bytes != null);

		Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
		Sheet sheet = wb.getSheetAt(0);

		Assert.assertEquals("Name", sheet.getRow(0).getCell(0).getStringCellValue());
		Assert.assertEquals("Week 1", sheet.getRow(0).getCell(1).getStringCellValue());
		Assert.assertEquals("Week 2", sheet.getRow(0).getCell(2).getStringCellValue());
		Assert.assertEquals("Week 3", sheet.getRow(0).getCell(3).getStringCellValue());
		Assert.assertEquals("Week 4", sheet.getRow(0).getCell(4).getStringCellValue());
		Assert.assertEquals("Row total", sheet.getRow(0).getCell(5).getStringCellValue());

		// check the first row
		Assert.assertEquals(1, (int) sheet.getRow(1).getCell(0).getNumericCellValue());
		Assert.assertEquals(1, (int) sheet.getRow(1).getCell(1).getNumericCellValue());
		Assert.assertEquals(2, (int) sheet.getRow(1).getCell(2).getNumericCellValue());
		Assert.assertEquals(3, (int) sheet.getRow(1).getCell(3).getNumericCellValue());
		Assert.assertEquals(4, (int) sheet.getRow(1).getCell(4).getNumericCellValue());
		Assert.assertEquals(10, (int) sheet.getRow(1).getCell(5).getNumericCellValue());

		// check the second row
		Assert.assertEquals(2, (int) sheet.getRow(2).getCell(0).getNumericCellValue());
		Assert.assertEquals(5, (int) sheet.getRow(2).getCell(1).getNumericCellValue());
		Assert.assertEquals(6, (int) sheet.getRow(2).getCell(2).getNumericCellValue());
		Assert.assertEquals(7, (int) sheet.getRow(2).getCell(3).getNumericCellValue());
		Assert.assertEquals(8, (int) sheet.getRow(2).getCell(4).getNumericCellValue());
		Assert.assertEquals(26, (int) sheet.getRow(2).getCell(5).getNumericCellValue());

		wb.close();

		// CSV
		bytes = template.process(false);
		Assert.assertTrue(bytes != null);

	}

	@Test
	public void testAverage() throws IOException {

		ExportPivotTemplate<Integer, Series, Integer, Week> template = new ExportPivotTemplate<Integer, Series, Integer, Week>(
		        seriesService, orders, filter, null, Lists.newArrayList("Name"), "Title", true, null) {

			@Override
			protected void setXlsCellValues(Row row, Series entity) {
				createCell(row, 0, entity, null, null).setCellValue(entity.getUnit());
			}

			@Override
			protected void setCsvCellValues(List<String> row, Series entity) {
				row.add(Integer.toString(entity.getUnit()));
			}

			@Override
			protected Object getValue(Series entity) {
				return entity.getValue();
			}

			@Override
			protected String getRowTotalCaption() {
				return "Row average";
			}

			@Override
			protected Object getRowId(Series entity) {
				return entity.getUnit();
			}

			@Override
			protected List<Week> getColumns() {
				return weeks;
			}

			@Override
			protected String getColumnHeader(Week u) {
				return u.getName();
			}

			@Override
			protected boolean createSumColumn() {
				return false;
			}

			@Override
			protected boolean createAveragesColumn() {
				return true;
			}

			@Override
			public int getPageSize() {
				return 1000;
			}

			@Override
			protected boolean columnValueMatches(Series entity, Week column) {
				return true;
			}
		};
		byte[] bytes = template.process(true);
		Assert.assertTrue(bytes != null);

		Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
		Sheet sheet = wb.getSheetAt(0);

		Assert.assertEquals("Name", sheet.getRow(0).getCell(0).getStringCellValue());
		Assert.assertEquals("Week 1", sheet.getRow(0).getCell(1).getStringCellValue());
		Assert.assertEquals("Week 2", sheet.getRow(0).getCell(2).getStringCellValue());
		Assert.assertEquals("Week 3", sheet.getRow(0).getCell(3).getStringCellValue());
		Assert.assertEquals("Week 4", sheet.getRow(0).getCell(4).getStringCellValue());
		Assert.assertEquals("Row average", sheet.getRow(0).getCell(5).getStringCellValue());

		// check the first row
		Assert.assertEquals(1, (int) sheet.getRow(1).getCell(0).getNumericCellValue());
		Assert.assertEquals(1, (int) sheet.getRow(1).getCell(1).getNumericCellValue());
		Assert.assertEquals(2, (int) sheet.getRow(1).getCell(2).getNumericCellValue());
		Assert.assertEquals(3, (int) sheet.getRow(1).getCell(3).getNumericCellValue());
		Assert.assertEquals(4, (int) sheet.getRow(1).getCell(4).getNumericCellValue());
		Assert.assertEquals("2,50", sheet.getRow(1).getCell(5).getStringCellValue());

		// check the second row
		Assert.assertEquals(2, (int) sheet.getRow(2).getCell(0).getNumericCellValue());
		Assert.assertEquals(5, (int) sheet.getRow(2).getCell(1).getNumericCellValue());
		Assert.assertEquals(6, (int) sheet.getRow(2).getCell(2).getNumericCellValue());
		Assert.assertEquals(7, (int) sheet.getRow(2).getCell(3).getNumericCellValue());
		Assert.assertEquals(8, (int) sheet.getRow(2).getCell(4).getNumericCellValue());
		Assert.assertEquals("6,50", sheet.getRow(2).getCell(5).getStringCellValue());

		wb.close();

		// CSV
		bytes = template.process(false);
		Assert.assertTrue(bytes != null);

	}

	@Test
	public void testBigDecimalSum() throws IOException {

		ExportPivotTemplate<Integer, Series, Integer, Week> template = new ExportPivotTemplate<Integer, Series, Integer, Week>(
		        seriesService, orders, filter, null, Lists.newArrayList("Name"), "Title", true, null) {

			@Override
			protected void setXlsCellValues(Row row, Series entity) {
				createCell(row, 0, entity, null, null).setCellValue(entity.getUnit());
			}

			@Override
			protected void setCsvCellValues(List<String> row, Series entity) {
				row.add(Integer.toString(entity.getUnit()));
			}

			@Override
			protected Object getValue(Series entity) {
				return entity.getBdValue();
			}

			@Override
			protected String getRowTotalCaption() {
				return "Row total";
			}

			@Override
			protected Object getRowId(Series entity) {
				return entity.getUnit();
			}

			@Override
			protected List<Week> getColumns() {
				return weeks;
			}

			@Override
			protected String getColumnHeader(Week u) {
				return u.getName();
			}

			@Override
			protected boolean createSumColumn() {
				return true;
			}

			@Override
			protected boolean createAveragesColumn() {
				return false;
			}

			@Override
			protected boolean usePercentages() {
				return true;
			}

			@Override
			public int getPageSize() {
				return 1000;
			}

			@Override
			protected boolean columnValueMatches(Series entity, Week column) {
				return true;
			}
		};
		byte[] bytes = template.process(true);
		Assert.assertTrue(bytes != null);

		Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes));
		Sheet sheet = wb.getSheetAt(0);

		Assert.assertEquals("Name", sheet.getRow(0).getCell(0).getStringCellValue());
		Assert.assertEquals("Week 1", sheet.getRow(0).getCell(1).getStringCellValue());
		Assert.assertEquals("Week 2", sheet.getRow(0).getCell(2).getStringCellValue());
		Assert.assertEquals("Week 3", sheet.getRow(0).getCell(3).getStringCellValue());
		Assert.assertEquals("Week 4", sheet.getRow(0).getCell(4).getStringCellValue());
		Assert.assertEquals("Row total", sheet.getRow(0).getCell(5).getStringCellValue());

		// check the first row
		Assert.assertEquals(1, (int) sheet.getRow(1).getCell(0).getNumericCellValue());
		Assert.assertEquals(1.0, sheet.getRow(1).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(2.0, sheet.getRow(1).getCell(2).getNumericCellValue(), 0.001);
		Assert.assertEquals(3.0, sheet.getRow(1).getCell(3).getNumericCellValue(), 0.001);
		Assert.assertEquals(4.0, sheet.getRow(1).getCell(4).getNumericCellValue(), 0.001);
		Assert.assertEquals(10.0, sheet.getRow(1).getCell(5).getNumericCellValue(), 0.001);

		// check the second row
		Assert.assertEquals(2, (int) sheet.getRow(2).getCell(0).getNumericCellValue());
		Assert.assertEquals(5.0, sheet.getRow(2).getCell(1).getNumericCellValue(), 0.001);
		Assert.assertEquals(6.0, sheet.getRow(2).getCell(2).getNumericCellValue(), 0.001);
		Assert.assertEquals(7.0, sheet.getRow(2).getCell(3).getNumericCellValue(), 0.001);
		Assert.assertEquals(8.0, sheet.getRow(2).getCell(4).getNumericCellValue(), 0.001);
		Assert.assertEquals(26.0, sheet.getRow(2).getCell(5).getNumericCellValue(), 0.001);

		wb.close();

		// CSV
		bytes = template.process(false);
		Assert.assertTrue(bytes != null);

	}

	@SuppressWarnings("unused")
	private class Series extends AbstractEntity<Integer> {

		private static final long serialVersionUID = -8290146926607114563L;

		private Integer id;

		private Integer value;

		private BigDecimal bdValue;

		private Integer unit;

		private Week week;

		private Series(Integer id, int value, BigDecimal bdValue, Integer unit, Week week) {
			this.id = id;
			this.value = value;
			this.unit = unit;
			this.week = week;
			this.bdValue = bdValue;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}

		public Integer getUnit() {
			return unit;
		}

		public void setUnit(Integer unit) {
			this.unit = unit;
		}

		public Week getWeek() {
			return week;
		}

		public void setWeek(Week week) {
			this.week = week;
		}

		public BigDecimal getBdValue() {
			return bdValue;
		}

		public void setBdValue(BigDecimal bdValue) {
			this.bdValue = bdValue;
		}
	}

	@SuppressWarnings("unused")
	private class Week extends AbstractEntity<Integer> {

		private static final long serialVersionUID = -8001840977221179054L;

		private Integer id;

		private String name;

		public Week(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}
