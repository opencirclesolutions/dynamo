package com.ocs.dynamo.ui.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

public class EmailValidatorTest {

    @Test
    public void test() {
        EmailValidator validator = new EmailValidator("message");

        ValidationResult res = validator.apply(null, new ValueContext());
        assertFalse(res.isError());

        res = validator.apply("", new ValueContext());
        assertFalse(res.isError());

        res = validator.apply("test@test.nl", new ValueContext());
        assertFalse(res.isError());

        res = validator.apply("bogus", new ValueContext());
        assertTrue(res.isError());
    }
}
