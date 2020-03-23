package com.ocs.dynamo.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.flow.data.binder.Result;

public class ConvertUtilsTest extends BaseMockitoTest {

    private EntityModelFactory emf = new EntityModelFactoryImpl();

    @BeforeAll
    public static void beforeClass() {
        System.setProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME, "com.ocs.dynamo.ui.SpringTestServiceLocator");
        System.setProperty("ocs.default.locale", "de");
    }

    @Test
    public void testConvertSearchValue() {
        EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

        Result<?> result = ConvertUtils.convertToModelValue(model.getAttributeModel("age"), "12");
        Object obj = result.getOrThrow(r -> new OCSRuntimeException());
        assertTrue(obj instanceof Long);
        assertEquals(12L, ((Long) obj).longValue());

        result = ConvertUtils.convertToModelValue(model.getAttributeModel("discount"), "12,34");
        obj = result.getOrThrow(r -> new OCSRuntimeException());
        assertTrue(obj instanceof BigDecimal);
        assertEquals(12.34, ((BigDecimal) obj).doubleValue(), 0.0001);

        result = ConvertUtils.convertToModelValue(model.getAttributeModel("id"), "17");
        obj = result.getOrThrow(r -> new OCSRuntimeException());
        assertTrue(obj instanceof Integer);
        assertEquals(17, ((Integer) obj).intValue());

        result = ConvertUtils.convertToModelValue(model.getAttributeModel("birthWeek"), "2015-05");
        obj = result.getOrThrow(r -> new OCSRuntimeException());
        assertTrue(obj instanceof LocalDate);
        assertEquals(LocalDate.of(2015, 1, 26), obj);
    }

    @Test
    public void testConvertToPresentationValue() {
        EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

        Object s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("age"), 12L);
        assertEquals("12", s);

        s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("discount"), BigDecimal.valueOf(17.79));
        assertEquals("17,79", s);

        s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("id"), 1234);
        assertEquals("1234", s);

        s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("birthWeek"), DateUtils.createLocalDate("01022015"));
        assertEquals("2015-05", s);

        s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("someDouble"), 1234.56);
        assertEquals("1234,56", s);
    }

    @Test
    public void testConvertToPresentationDates() {
        EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

        // LocalDate
        Object s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("birthDate"), LocalDate.of(2014, 1, 1));
        assertEquals("01-01-2014", ((LocalDate) s).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        // LocalDateTime
        s = ConvertUtils.convertToPresentationValue(model.getAttributeModel("registrationTime"), LocalDateTime.of(2014, 1, 1, 13, 14, 0));
        assertEquals("2014-01-01T13:14", s.toString());
    }
}
