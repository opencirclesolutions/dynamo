package com.ocs.dynamo.domain.model.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class URLValidatorTest {

    @Test
    public void test() {

        URLValidator validator = new URLValidator();
        assertThat(validator.isValid("https://hackertyper.net", null)).isTrue();

    }
}