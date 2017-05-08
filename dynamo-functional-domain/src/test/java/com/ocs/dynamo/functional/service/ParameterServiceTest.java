package com.ocs.dynamo.functional.service;

import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.ParameterType;
import com.ocs.dynamo.test.BaseIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by R.E.M. Claassen on 10-4-2017.
 */
public class ParameterServiceTest extends BaseIntegrationTest {

    @Inject
    ParameterServiceImpl parameterServiceImpl;

    private Parameter maxPrograms;
    private Parameter showMargins;
    private Parameter insufficientFunds;

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

        insufficientFunds = new Parameter();
        insufficientFunds.setName("insufficientFunds");
        insufficientFunds.setParameterType(ParameterType.STRING);
        insufficientFunds.setValue("Insufficient funds");
        getEntityManager().persist(insufficientFunds);
    }

    @Test
    public void returnNullWhenIncorrectValue() {
        Assert.assertNull(parameterServiceImpl.getValueAsString("showMargins"));
        Assert.assertNull(parameterServiceImpl.getValueAsInteger("insufficientFunds"));
        Assert.assertEquals(Boolean.FALSE, parameterServiceImpl.getValueAsBoolean("maximumPrograms"));
    }

    @Test
    public void returnCorrectValueAndType() {
        Assert.assertEquals(Integer.valueOf(12), parameterServiceImpl.getValueAsInteger("maximumPrograms"));
        Assert.assertEquals(Boolean.valueOf(true), parameterServiceImpl.getValueAsBoolean("showMargins"));
        Assert.assertEquals("Insufficient funds", parameterServiceImpl.getValueAsString("insufficientFunds"));
    }

    @Test
    public void returnNullWhenNoParameterFound() {
        Assert.assertNull(parameterServiceImpl.getValueAsString("maverick"));
        Assert.assertNull(parameterServiceImpl.getValueAsInteger("maverick"));
        Assert.assertEquals(Boolean.FALSE, parameterServiceImpl.getValueAsBoolean("maverick"));
    }

}