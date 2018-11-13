package com.ocs.dynamo.ui.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.utils.DateUtils;
import com.vaadin.data.Result;

public class ConvertUtilTest {

	private EntityModelFactory emf = new EntityModelFactoryImpl();

	@BeforeClass
	public static void beforeClass() {
		System.setProperty("ocs.default.locale", "de");
	}

	@Test
	public void testConvertSearchValue() {
		EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

		Result<?> result = ConvertUtil.convertToModelValue(model.getAttributeModel("age"), "12");
		Object obj = result.getOrThrow(r -> new OCSRuntimeException());
		Assert.assertTrue(obj instanceof Long);
		Assert.assertEquals(12L, ((Long) obj).longValue());

		result = ConvertUtil.convertToModelValue(model.getAttributeModel("discount"), "12,34");
		obj = result.getOrThrow(r -> new OCSRuntimeException());
		Assert.assertTrue(obj instanceof BigDecimal);
		Assert.assertEquals(12.34, ((BigDecimal) obj).doubleValue(), 0.0001);

		result = ConvertUtil.convertToModelValue(model.getAttributeModel("id"), "17");
		obj = result.getOrThrow(r -> new OCSRuntimeException());
		Assert.assertTrue(obj instanceof Integer);
		Assert.assertEquals(17, ((Integer) obj).intValue());

		result = ConvertUtil.convertToModelValue(model.getAttributeModel("birthWeek"), "2015-05");
		obj = result.getOrThrow(r -> new OCSRuntimeException());
		Assert.assertTrue(obj instanceof LocalDate);
		Assert.assertEquals(LocalDate.of(2015, 1, 26), obj);
	}

	@Test
	public void testConvertToPresentationValue() {
		EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

		Object s = ConvertUtil.convertToPresentationValue(model.getAttributeModel("age"), 12L);
		Assert.assertEquals("12", s);

		s = ConvertUtil.convertToPresentationValue(model.getAttributeModel("discount"), BigDecimal.valueOf(17.79));
		Assert.assertEquals("17,79", s);

		s = ConvertUtil.convertToPresentationValue(model.getAttributeModel("id"), 1234);
		Assert.assertEquals("1234", s);

		s = ConvertUtil.convertToPresentationValue(model.getAttributeModel("birthWeek"),
				DateUtils.createLocalDate("01022015"));
		Assert.assertEquals("2015-05", s);
	}

	@Test
	public void testConvertToPresentationDates() {
		EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

		// LocalDate
		Object s = ConvertUtil.convertToPresentationValue(model.getAttributeModel("birthDate"),
				LocalDate.of(2014, 1, 1));
		Assert.assertEquals("01-01-2014", ((LocalDate) s).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

		// LocalDateTime
		s = ConvertUtil.convertToPresentationValue(model.getAttributeModel("registrationTime"),
				LocalDateTime.of(2014, 1, 1, 13, 14, 0));
		Assert.assertEquals("2014-01-01T13:14", s.toString());
	}
}
