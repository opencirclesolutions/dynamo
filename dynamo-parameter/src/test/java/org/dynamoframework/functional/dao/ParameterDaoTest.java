package org.dynamoframework.functional.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.dynamoframework.BackendIntegrationTest;
import org.dynamoframework.functional.ParameterIntegrationTestConfig;
import org.dynamoframework.functional.domain.Parameter;
import org.dynamoframework.functional.domain.ParameterType;

/**
 * @author Bas Rutten
 */
@SpringBootTest(classes = ParameterIntegrationTestConfig.class)
public class ParameterDaoTest extends BackendIntegrationTest {

	@Autowired
	private ParameterDao parameterDao;

	private Parameter maxPrograms;

	private Parameter showMargins;

	@Autowired
	private ValidatorFactory factory;

	@BeforeEach
	public void setup() {
		maxPrograms = new Parameter();
		maxPrograms.setName("maximumPrograms");
		maxPrograms.setParameterType(ParameterType.INTEGER);
		maxPrograms.setValue("12");
		getEntityManager().persist(maxPrograms);

		showMargins = new Parameter();
		showMargins.setName("showMargins");
		showMargins.setParameterType(ParameterType.BOOLEAN);
		showMargins.setValue("true");
		getEntityManager().persist(showMargins);
	}


	@Test
	public void getAll() {
		List<Parameter> all = parameterDao.findAll();
		assertEquals(2, all.size());
	}

	@Test
	public void notAllowedToSaveValueOfIncorrectTypeInt() {
		maxPrograms.setValue("not a number");

		Validator val = factory.getValidator();
		Set<ConstraintViolation<Parameter>> errors = val.validate(maxPrograms);
		assertTrue(errors.size() > 0);

		maxPrograms.setValue("2");
		errors = val.validate(maxPrograms);
		assertTrue(errors.size() == 0);
	}

	@Test
	public void notAllowedToSaveValueOfIncorrectTypeBoolean() {
		Parameter showPercentage = new Parameter();
		showPercentage.setParameterType(ParameterType.BOOLEAN);

		showPercentage.setValue("5");
		showPercentage.setName("showPercentage");

		Validator val = factory.getValidator();
		Set<ConstraintViolation<Parameter>> errors = val.validate(showPercentage);
		assertTrue(errors.size() > 0);

		showPercentage.setValue("true");
		errors = val.validate(showPercentage);
		assertTrue(errors.size() == 0);
	}

}
