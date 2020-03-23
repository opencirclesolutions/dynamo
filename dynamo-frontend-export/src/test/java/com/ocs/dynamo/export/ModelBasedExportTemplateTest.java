package com.ocs.dynamo.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity.TestEnum;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.TestEntityService;
import com.ocs.dynamo.ui.FrontendIntegrationTest;
import com.ocs.dynamo.ui.FrontendIntegrationTestConfig;
import com.ocs.dynamo.ui.composite.export.impl.ModelBasedCsvExportTemplate;
import com.ocs.dynamo.ui.composite.export.impl.ModelBasedExcelExportTemplate;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.utils.DateUtils;

@SpringBootTest(classes = FrontendIntegrationTestConfig.class)
public class ModelBasedExportTemplateTest extends FrontendIntegrationTest {

    private EntityModelFactory entityModelFactory = new EntityModelFactoryImpl();

    @Autowired
    private TestEntityService testEntityService;

    private TestEntity e1;

    private TestEntity e2;

    @BeforeEach
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
        e1.setRegistrationTime(DateUtils.createLocalTime("111213"));
        e1.setZoned(ZonedDateTime.of(DateUtils.createLocalDateTime("14082015 111213"), ZoneId.of("CET")));
        e1.setSomeDouble(44.44);

        e2 = new TestEntity("Harry", 12L);
        e2.setRate(BigDecimal.valueOf(3));
        e2 = testEntityService.save(e2);

        entityModelFactory.getModel(TestEntity.class);
    }

    @Test
    public void testExcel() throws IOException {
        ModelBasedExcelExportTemplate<Integer, TestEntity> template = new ModelBasedExcelExportTemplate<Integer, TestEntity>(
                testEntityService, entityModelFactory.getModel(TestEntity.class), ExportMode.ONLY_VISIBLE_IN_GRID,
                new SortOrder[] { new SortOrder("name", Direction.ASC) }, null, "Sheet name", null);
        byte[] bytes = template.process();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("Sheet name", wb.getSheetName(0));

            // check the header row
            int i = 0;
            assertEquals("Age", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Birth Date", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Birth Week", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Discount", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Name", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Rate", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Registration Time", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Boolean", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Boolean2", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Double", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Enum", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Int", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some String", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Text Area", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Some Time", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Url", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("Zoned", sheet.getRow(0).getCell(i++).getStringCellValue());

            // check the data row
            Row row = sheet.getRow(1);
            i = 0;
            assertEquals(11, row.getCell(0).getNumericCellValue(), 0.001);

            // assertEquals("2014-14", row.getCell(2).getStringCellValue());
            assertEquals(34, row.getCell(3).getNumericCellValue(), 0.001);
            assertEquals("Bob", row.getCell(4).getStringCellValue());
            assertEquals(0.04, row.getCell(5).getNumericCellValue(), 0.001);

            assertEquals("false", row.getCell(7).getStringCellValue());
            assertEquals("On", row.getCell(8).getStringCellValue());
            assertEquals(44.44, row.getCell(9).getNumericCellValue(), 0.001);
            assertEquals("Value A", row.getCell(10).getStringCellValue());
            assertEquals(1234, row.getCell(11).getNumericCellValue(), 0.001);
            assertEquals("abab", row.getCell(13).getStringCellValue());
            assertEquals("http://www.google.nl", row.getCell(15).getStringCellValue());
            assertEquals("14-08-2015 11:12:13+0200", row.getCell(16).getStringCellValue());
        }

    }

    @Test
    public void testCsv() {
        ModelBasedCsvExportTemplate<Integer, TestEntity> template = new ModelBasedCsvExportTemplate<Integer, TestEntity>(testEntityService,
                entityModelFactory.getModel(TestEntity.class), ExportMode.ONLY_VISIBLE_IN_GRID,
                new SortOrder[] { new SortOrder("name", Direction.ASC) }, null, null);
        byte[] bytes = template.process();
        String str = new String(bytes);
        String[] lines = str.split("\n");

        assertEquals(
                "\"Age\";\"Birth Date\";\"Birth Week\";\"Discount\";\"Name\";\"Rate\";\"Registration Time\";\"Some Boolean\";\"Some Boolean2\";\"Some Double\";\"Some Enum\";\"Some Int\";\"Some String\";\"Some Text Area\";\"Some Time\";\"Url\";\"Zoned\"",
                lines[0].trim());
        assertEquals(
                "\"11\";\"01/04/2014\";\"2014-14\";\"34,00\";\"Bob\";\"4,00%\";\"11:12:13\";\"false\";\"On\";\"44,44\";\"Value A\";\"1.234\";\"some\";\"abab\";\"12:13:14\";\"http://www.google.nl\";\"14-08-2015 11:12:13+0200\"",
                lines[1].trim());
    }
}
