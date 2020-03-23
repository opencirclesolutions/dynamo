package com.ocs.dynamo.domain.model.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class URLValidatorTest {

    private URLValidator validator = new URLValidator();

    @Test
    public void testNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    public void testValid() {
        assertTrue(validator.isValid("http://www.google.nl", null));
        assertTrue(validator.isValid("www.google.nl", null));
        assertTrue(validator.isValid("mijn.site.nl", null));
        assertFalse(validator.isValid("mijn.site", null));
    }

    @Test
    public void testValid_basic() {
        assertFalse(validator.isValid("nowwwnonothing", null));
        assertFalse(validator.isValid("no-www-no-nothing", null));
        assertFalse(validator.isValid("no/www/no/nothing", null));
        assertFalse(validator.isValid("no$www$no$nothing", null));
        assertFalse(validator.isValid("no@www@no@nothing", null));
        assertFalse(validator.isValid("no www no nothing", null));
    }

    @Test
    public void testValid_exoticCharacters() {
        assertTrue(validator.isValid("http://www.goqsùdfù^ùê   ù^zù'^^\"'t\"'^m§\"'ogle.nl", null));
    }

}
