package com.ocs.dynamo.export;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.commons.io.FileUtils;
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
import com.ocs.dynamo.utils.DateUtils;

public class ModelBasedExportTemplateTest extends BaseIntegrationTest {

	private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

	@Autowired
	private TestEntityService testEntityService;

	private TestEntity e1;

	private TestEntity e2;

	@Before
	public void setup() {
		e1 = new TestEntity("Bob", 11L);
		e1.setRate(BigDecimal.valueOf(4));
		e1.setBirthDate(DateUtils.createDate("01042014"));
		e1.setBirthWeek(DateUtils.createDate("01042014"));
		e1.setDiscount(BigDecimal.valueOf(34));
		e1.setSomeBoolean(false);
		e1.setSomeBoolean2(true);
		e1.setSomeEnum(TestEnum.A);
		e1.setSomeInt(1234);
		e1.setSomeTextArea("abab");
		e1.setSomeTime(DateUtils.createTime("121314"));
		e1.setUrl("http://www.google.nl");
		e1 = testEntityService.save(e1);

		e2 = new TestEntity("Harry", 12L);
		e2.setRate(BigDecimal.valueOf(3));
		e2 = testEntityService.save(e2);

		entityModelFactory.getModel(TestEntity.class);
	}

	@Test
	public void testExcel() throws IOException {
		ModelBasedExportTemplate<Integer, TestEntity> template = new ModelBasedExportTemplate<Integer, TestEntity>(
		        testEntityService, entityModelFactory.getModel(TestEntity.class), null, null, "Sheet name", true, null) {

			@Override
			public int getPageSize() {
				return 10000;
			}
		};
		byte[] bytes = template.process(true);

		try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
			Sheet sheet = wb.getSheetAt(0);
			Assert.assertEquals("Sheet name", wb.getSheetName(0));

			// check the header row
			Assert.assertEquals("Age", sheet.getRow(0).getCell(0).getStringCellValue());
			Assert.assertEquals("Birth Date", sheet.getRow(0).getCell(1).getStringCellValue());
			Assert.assertEquals("Birth Week", sheet.getRow(0).getCell(2).getStringCellValue());
			Assert.assertEquals("Discount", sheet.getRow(0).getCell(3).getStringCellValue());
			Assert.assertEquals("Name", sheet.getRow(0).getCell(4).getStringCellValue());
			Assert.assertEquals("Parent", sheet.getRow(0).getCell(5).getStringCellValue());
			Assert.assertEquals("Rate", sheet.getRow(0).getCell(6).getStringCellValue());
			Assert.assertEquals("Some Boolean", sheet.getRow(0).getCell(7).getStringCellValue());
			Assert.assertEquals("Some Boolean2", sheet.getRow(0).getCell(8).getStringCellValue());
			Assert.assertEquals("Some Enum", sheet.getRow(0).getCell(9).getStringCellValue());
			Assert.assertEquals("Some Int", sheet.getRow(0).getCell(10).getStringCellValue());
			Assert.assertEquals("Some String", sheet.getRow(0).getCell(11).getStringCellValue());
			Assert.assertEquals("Some Text Area", sheet.getRow(0).getCell(12).getStringCellValue());
			Assert.assertEquals("Some Time", sheet.getRow(0).getCell(13).getStringCellValue());
			Assert.assertEquals("Url", sheet.getRow(0).getCell(15).getStringCellValue());

			// check the data row
			Row row = sheet.getRow(1);
			Assert.assertEquals(11, row.getCell(0).getNumericCellValue(), 0.001);
			Assert.assertEquals(DateUtils.createDate("01042014"), row.getCell(1).getDateCellValue());
			Assert.assertEquals("2014-14", row.getCell(2).getStringCellValue());
			Assert.assertEquals(34, row.getCell(3).getNumericCellValue(), 0.001);
			Assert.assertEquals("Bob", row.getCell(4).getStringCellValue());
			Assert.assertEquals(0.04, row.getCell(6).getNumericCellValue(), 0.001);
			Assert.assertEquals(false, row.getCell(7).getBooleanCellValue());
			Assert.assertEquals("On", row.getCell(8).getStringCellValue());
			Assert.assertEquals("Value A", row.getCell(9).getStringCellValue());
			Assert.assertEquals(1234, row.getCell(10).getNumericCellValue(), 0.001);
			Assert.assertEquals("abab", row.getCell(12).getStringCellValue());
			Assert.assertEquals("http://www.google.nl", row.getCell(15).getStringCellValue());
		}

	}

	@Test
	public void testCsv() {
		ModelBasedExportTemplate<Integer, TestEntity> template = new ModelBasedExportTemplate<Integer, TestEntity>(
		        testEntityService, entityModelFactory.getModel(TestEntity.class), null, null, "Sheet name", true, null) {
			@Override
			public int getPageSize() {
				return 10000;
			}
		};
		byte[] bytes = template.process(false);
		String str = new String(bytes);
		String[] lines = str.split("\n");

		Assert.assertEquals(
		        "\"Age\";\"Birth Date\";\"Birth Week\";\"Discount\";\"Name\";\"Parent\";\"Rate\";\"Some Boolean\";\"Some Boolean2\";\"Some Enum\";\"Some Int\";\"Some String\";\"Some Text Area\";\"Some Time\";\"Test Domain\";\"Url\"",
		        lines[0]);
		Assert.assertEquals(
		        "\"11\";\"01/04/2014\";\"2014-14\";\"34,00\";\"Bob\";;\"4,00%\";;\"On\";\"Value A\";\"1.234\";;\"abab\";\"12:13:14\";;\"http://www.google.nl\"",
		        lines[1]);
	}
}
