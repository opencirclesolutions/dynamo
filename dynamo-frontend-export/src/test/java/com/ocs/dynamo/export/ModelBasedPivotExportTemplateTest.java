package com.ocs.dynamo.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

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
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.export.impl.ModelBasedCsvPivotExportTemplate;
import com.ocs.dynamo.ui.composite.export.impl.ModelBasedExcelPivotExportTemplate;
import com.ocs.dynamo.utils.DateUtils;

@SpringBootTest(classes = FrontendIntegrationTestConfig.class)
public class ModelBasedPivotExportTemplateTest extends FrontendIntegrationTest {

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
        e1.setRegistrationTime(DateUtils.createLocalTime("111213"));
        e1.setZoned(ZonedDateTime.of(DateUtils.createLocalDateTime("14082015 111213"), ZoneId.of("CET")));
        e1.setSomeDouble(44.44);
        e1 = testEntityService.save(e1);

        e2 = new TestEntity("Bob", 11L);
        e2.setRate(BigDecimal.valueOf(4));
        e2.setBirthDate(DateUtils.createLocalDate("01042014"));
        e2.setBirthWeek(DateUtils.createLocalDate("01042014"));
        e2.setDiscount(BigDecimal.valueOf(34));
        e2.setSomeBoolean(false);
        e2.setSomeBoolean2(true);
        e2.setSomeEnum(TestEnum.B);
        e2.setSomeInt(1234);
        e2.setSomeString("some");
        e2.setSomeTextArea("abab");
        e2.setSomeTime(DateUtils.createLocalTime("121314"));
        e2.setUrl("http://www.google.nl");
        e2.setRegistrationTime(DateUtils.createLocalTime("111213"));
        e2.setZoned(ZonedDateTime.of(DateUtils.createLocalDateTime("14082015 111213"), ZoneId.of("CET")));
        e2.setSomeDouble(44.44);
        e2 = testEntityService.save(e2);

    }

    @Test
    public void testExcel() throws IOException {

        PivotParameters pars = createPivotParameters();

        ModelBasedExcelPivotExportTemplate<Integer, TestEntity> template = new ModelBasedExcelPivotExportTemplate<Integer, TestEntity>(
                testEntityService, entityModelFactory.getModel(TestEntity.class),
                new SortOrder[] { new SortOrder("name", Direction.ASC), new SortOrder("someEnum", Direction.ASC) }, null, "Sheet name",
                null, pars);
        byte[] bytes = template.process();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("Sheet name", wb.getSheetName(0));

            // check the header row
            int i = 0;
            assertEquals("name", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("A", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("B", sheet.getRow(0).getCell(i++).getStringCellValue());
            assertEquals("C", sheet.getRow(0).getCell(i++).getStringCellValue());

            // check the data row
            Row row = sheet.getRow(1);
            i = 0;
            assertEquals("Bob", row.getCell(i++).getStringCellValue());
            assertEquals("A", row.getCell(i++).getStringCellValue());
            assertEquals("B", row.getCell(i++).getStringCellValue());
            // no value for testEnum C, hence a
            assertNull(row.getCell(i++));
        }

    }

    @Test
    public void testCsv() {

        PivotParameters pars = createPivotParameters();

        ModelBasedCsvPivotExportTemplate<Integer, TestEntity> template = new ModelBasedCsvPivotExportTemplate<Integer, TestEntity>(
                testEntityService, entityModelFactory.getModel(TestEntity.class),
                new SortOrder[] { new SortOrder("name", Direction.ASC), new SortOrder("someEnum", Direction.ASC) }, null, "Sheet name",
                pars);
        byte[] bytes = template.process();

        String str = new String(bytes);
        String[] lines = str.split("\n");

        assertEquals("\"name\";\"A\";\"B\";\"C\"", lines[0].trim());
        assertEquals("\"Bob\";\"A\";\"B\";\"\"", lines[1].trim());
    }

    private PivotParameters createPivotParameters() {
        PivotParameters pars = new PivotParameters();
        pars.setFixedColumnKeys(List.of("name"));
        pars.setRowKeyProperty("age");
        pars.setPivotedProperties(List.of("someEnum"));
        pars.setColumnKeyProperty("someEnum");
        pars.setPossibleColumnKeys(List.of(TestEnum.values()));
        return pars;
    }

}
