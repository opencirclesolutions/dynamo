package com.ocs.dynamo.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.composite.export.impl.ModelBasedCsvExportTemplate;
import com.ocs.dynamo.ui.composite.export.impl.ModelBasedExcelExportTemplate;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.utils.DateUtils;

public class ModelBasedExportTemplateTest extends BaseIntegrationTest {

	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	@Autowired
	private TestEntityService testEntityService;

	private TestEntity e1;

	private TestEntity e2;

	@Before
	public void setup() {
		System.setProperty("ocs.default.locale", "de");
		e1 = new TestEntity("Bob", 11L);
		e1.setRate(BigDecimal.valueOf(4));
		e1.setBirthDate(DateUtils.createLocalDate("01042014"));
		e1.setBirthWeek(DateUtils.createLocalDate("01042014"));
		e1.setDiscount(BigDecimal.valueOf(34));
		e1.setSomeBoolean(false);
		e1.setSomeBoolean2(true);
		e1.setSomeEnum(TestEnum.A);
		e1.setSomeInt(1234);
		e1.setSomeString("some");
		e1.setSomeTextArea("abab");
		e1.setSomeTime(DateUtils.createLocalTime("121314"));
		e1.setUrl("http://www.google.nl");
		e1 = testEntityService.save(e1);
		e1.setRegistrationTime(DateUtils.createLocalDateTime("14082015 111213"));
		e1.setZoned(ZonedDateTime.of(DateUtils.createLocalDateTime("14082015 111213"), ZoneId.of("CET")));

		e2 = new TestEntity("Harry", 12L);
		e2.setRate(BigDecimal.valueOf(3));
		e2 = testEntityService.save(e2);

		entityModelFactory.getModel(TestEntity.class);
	}

	@Test
	public void testExcel() throws IOException {
		ModelBasedExcelExportTemplate<Integer, TestEntity> template = new ModelBasedExcelExportTemplate<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), ExportMode.ONLY_VISIBLE_IN_TABLE,
				null, null, "Sheet name", null);
		byte[] bytes = template.process();

		try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
			Sheet sheet = wb.getSheetAt(0);
			Assert.assertEquals("Sheet name", wb.getSheetName(0));

			// check the header row
			int i = 0;
			Assert.assertEquals("Age", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Birth Date", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Birth Week", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Discount", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Name", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Rate", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Registration Time", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some Boolean", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some Boolean2", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some Enum", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some Int", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some String", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some Text Area", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Some Time", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Url", sheet.getRow(0).getCell(i++).getStringCellValue());
			Assert.assertEquals("Zoned", sheet.getRow(0).getCell(i++).getStringCellValue());

			// check the data row
			Row row = sheet.getRow(1);
			i = 0;
			Assert.assertEquals(11, row.getCell(0).getNumericCellValue(), 0.001);
			// Assert.assertEquals(DateUtils.createLocalDate("01042014"),
			// LocalDate.from(row.getCell(1).getDateCellValue().toInstant()));

			// Assert.assertEquals("2014-14", row.getCell(2).getStringCellValue());
			Assert.assertEquals(34, row.getCell(3).getNumericCellValue(), 0.001);
			Assert.assertEquals("Bob", row.getCell(4).getStringCellValue());
			Assert.assertEquals(0.04, row.getCell(5).getNumericCellValue(), 0.001);
			// Assert.assertEquals(DateUtils.createDateTime("14082015 111213"),
			// row.getCell(7).getDateCellValue());
			Assert.assertEquals("false", row.getCell(7).getStringCellValue());
			Assert.assertEquals("On", row.getCell(8).getStringCellValue());
			Assert.assertEquals("Value A", row.getCell(9).getStringCellValue());
			Assert.assertEquals(1234, row.getCell(10).getNumericCellValue(), 0.001);
			Assert.assertEquals("abab", row.getCell(12).getStringCellValue());
			Assert.assertEquals("http://www.google.nl", row.getCell(14).getStringCellValue());
			Assert.assertEquals("14-08-2015 11:12:13+0200", row.getCell(15).getStringCellValue());
		}

	}

	@Test
	public void testCsv() {
		ModelBasedCsvExportTemplate<Integer, TestEntity> template = new ModelBasedCsvExportTemplate<Integer, TestEntity>(
				testEntityService, entityModelFactory.getModel(TestEntity.class), ExportMode.ONLY_VISIBLE_IN_TABLE,
				null, null, "Sheet name", null);
		byte[] bytes = template.process();
		String str = new String(bytes);
		String[] lines = str.split("\n");

		Assert.assertEquals(
				"\"Age\";\"Birth Date\";\"Birth Week\";\"Discount\";\"Name\";\"Rate\";\"Registration Time\";\"Some Boolean\";\"Some Boolean2\";\"Some Enum\";\"Some Int\";\"Some String\";\"Some Text Area\";\"Some Time\";\"Url\";\"Zoned\"",
				lines[0].trim());
		Assert.assertEquals(
				"\"11\";\"01/04/2014\";\"2014-14\";\"34,00\";\"Bob\";\"4,00%\";\"14-08-2015 11:12:13\";\"false\";\"On\";\"Value A\";\"1.234\";\"some\";\"abab\";\"12:13:14\";\"http://www.google.nl\";\"14-08-2015 11:12:13+0200\"",
				lines[1].trim());
	}
}
