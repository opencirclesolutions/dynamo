package com.ocs.dynamo.functional.dao;

import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.ParameterType;
import com.ocs.dynamo.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;

/**
 * Created by R.E.M. Claassen on 6-4-2017.
 */
public class ParameterDaoTest extends BaseIntegrationTest {

	@Inject
	private ParameterDao parameterDao;

	private Parameter maxPrograms;

	private Parameter showMargins;

	@Inject
	private ValidatorFactory factory;

	@Before
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
		Assert.assertEquals(2, all.size());
	}

	@Test
	public void notAllowedToSaveValueOfIncorrectTypeInt() {
		maxPrograms.setValue("not a number");

		Validator val = factory.getValidator();
		Set<ConstraintViolation<Parameter>> errors = val.validate(maxPrograms);
		Assert.assertTrue(errors.size() > 0);

		maxPrograms.setValue("2");
		errors = val.validate(maxPrograms);
		Assert.assertTrue(errors.size() == 0);
	}

	@Test
	public void notAllowedToSaveValueOfIncorrectTypeBoolean() {
		Parameter showPercentage = new Parameter();
		showPercentage.setParameterType(ParameterType.BOOLEAN);

		showPercentage.setValue("5");
		showPercentage.setName("showPercentage");

		Validator val = factory.getValidator();
		Set<ConstraintViolation<Parameter>> errors = val.validate(showPercentage);
		Assert.assertTrue(errors.size() > 0);

		showPercentage.setValue("true");
		errors = val.validate(showPercentage);
		Assert.assertTrue(errors.size() == 0);
	}

}
