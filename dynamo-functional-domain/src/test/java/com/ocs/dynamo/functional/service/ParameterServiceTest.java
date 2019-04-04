package com.ocs.dynamo.functional.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ocs.dynamo.functional.FunctionalDomainIntegrationTestConfig;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.ParameterType;
import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * Created by R.E.M. Claassen on 10-4-2017.
 */
@SpringBootTest(classes = FunctionalDomainIntegrationTestConfig.class)
public class ParameterServiceTest extends BaseIntegrationTest {

    @Autowired
    private ParameterService parameterService;

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
        Assert.assertNull(parameterService.getValueAsString("showMargins"));
        Assert.assertNull(parameterService.getValueAsInteger("insufficientFunds"));
        Assert.assertEquals(Boolean.FALSE, parameterService.getValueAsBoolean("maximumPrograms"));
    }

    @Test
    public void returnCorrectValueAndType() {
        Assert.assertEquals(Integer.valueOf(12), parameterService.getValueAsInteger("maximumPrograms"));
        Assert.assertEquals(Boolean.valueOf(true), parameterService.getValueAsBoolean("showMargins"));
        Assert.assertEquals("Insufficient funds", parameterService.getValueAsString("insufficientFunds"));
    }

    @Test
    public void returnNullWhenNoParameterFound() {
        Assert.assertNull(parameterService.getValueAsString("maverick"));
        Assert.assertNull(parameterService.getValueAsInteger("maverick"));
        Assert.assertEquals(Boolean.FALSE, parameterService.getValueAsBoolean("maverick"));
    }

}