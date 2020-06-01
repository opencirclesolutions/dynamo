package com.ocs.dynamo.functional.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ocs.dynamo.functional.ParameterIntegrationTestConfig;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.ParameterType;
import com.ocs.dynamo.test.BaseIntegrationTest;

/**
 * Created by R.E.M. Claassen on 10-4-2017.
 */
@SpringBootTest(classes = ParameterIntegrationTestConfig.class)
public class ParameterServiceTest extends BaseIntegrationTest {

    @Autowired
    private ParameterService parameterService;

    private Parameter maxPrograms;

    private Parameter showMargins;

    private Parameter insufficientFunds;

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

        insufficientFunds = new Parameter();
        insufficientFunds.setName("insufficientFunds");
        insufficientFunds.setParameterType(ParameterType.STRING);
        insufficientFunds.setValue("Insufficient funds");
        getEntityManager().persist(insufficientFunds);
    }

    @Test
    public void returnNullWhenIncorrectValue() {
        assertNull(parameterService.getValueAsString("showMargins"));
        assertNull(parameterService.getValueAsInteger("insufficientFunds"));
        assertEquals(Boolean.FALSE, parameterService.getValueAsBoolean("maximumPrograms"));
    }

    @Test
    public void returnCorrectValueAndType() {
        assertEquals(Integer.valueOf(12), parameterService.getValueAsInteger("maximumPrograms"));
        assertEquals(Boolean.valueOf(true), parameterService.getValueAsBoolean("showMargins"));
        assertEquals("Insufficient funds", parameterService.getValueAsString("insufficientFunds"));
    }

    @Test
    public void returnNullWhenNoParameterFound() {
        assertNull(parameterService.getValueAsString("maverick"));
        assertNull(parameterService.getValueAsInteger("maverick"));
        assertEquals(Boolean.FALSE, parameterService.getValueAsBoolean("maverick"));
    }

}