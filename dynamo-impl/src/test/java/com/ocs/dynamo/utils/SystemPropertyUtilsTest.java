package com.ocs.dynamo.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class SystemPropertyUtilsTest {

    @BeforeEach
    public void setupEntityModelFactoryTest() {
        System.setProperty("ocs.default.date.format", "dd-MM-yyyy");
        System.setProperty("ocs.default.datetime.format", "dd-MM-yyyy HH:mm:ss");
    }

    @Test
    public void testDefaults() {
        assertEquals("dd-MM-yyyy", SystemPropertyUtils.getDefaultDateFormat());
        assertEquals("dd-MM-yyyy HH:mm:ss", SystemPropertyUtils.getDefaultDateTimeFormat());
        assertEquals(2, SystemPropertyUtils.getDefaultDecimalPrecision());
        assertEquals("HH:mm:ss", SystemPropertyUtils.getDefaultTimeFormat());

        assertEquals("\"", SystemPropertyUtils.getCsvQuoteChar());
        assertEquals(";", SystemPropertyUtils.getCsvSeparator());
        assertEquals("\"\"", SystemPropertyUtils.getCsvEscapeChar());

        Locale loc = new Locale.Builder().setLanguage("en").build();
        assertThat(SystemPropertyUtils.getDefaultLocale()).isEqualTo(loc);

        assertFalse(SystemPropertyUtils.getDefaultSearchCaseSensitive());
        assertFalse(SystemPropertyUtils.getDefaultSearchPrefixOnly());

        assertEquals("true", SystemPropertyUtils.getDefaultTrueRepresentation());
        assertEquals("false", SystemPropertyUtils.getDefaultFalseRepresentation());

        assertTrue(SystemPropertyUtils.isCapitalizeWords());
        assertTrue(SystemPropertyUtils.useDefaultPromptValue());
    }
}
