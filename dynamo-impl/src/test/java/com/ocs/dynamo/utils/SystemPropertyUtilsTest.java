package com.ocs.dynamo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.util.SystemPropertyUtils;

public class SystemPropertyUtilsTest {

    @Test
    public void testDefaults() {
        assertEquals("€", SystemPropertyUtils.getDefaultCurrencySymbol());
        assertEquals("dd-MM-yyyy", SystemPropertyUtils.getDefaultDateFormat());
        assertEquals("dd-MM-yyyy HH:mm:ss", SystemPropertyUtils.getDefaultDateTimeFormat());
        assertEquals(2, SystemPropertyUtils.getDefaultDecimalPrecision());
        assertEquals("HH:mm:ss", SystemPropertyUtils.getDefaultTimeFormat());

        assertEquals(false, SystemPropertyUtils.allowListExport());
        assertEquals("\"", SystemPropertyUtils.getCsvQuoteChar());
        assertEquals(";", SystemPropertyUtils.getCsvSeparator());
        assertEquals("\"\"", SystemPropertyUtils.getCsvEscapeChar());

        assertEquals(false, SystemPropertyUtils.useThousandsGroupingInEditMode());

        assertEquals("en_GB", SystemPropertyUtils.getDefaultLocale());

        assertFalse(SystemPropertyUtils.getDefaultSearchCaseSensitive());
        assertFalse(SystemPropertyUtils.getDefaultSearchPrefixOnly());

        assertEquals("true", SystemPropertyUtils.getDefaultTrueRepresentation());
        assertEquals("false", SystemPropertyUtils.getDefaultFalseRepresentation());

        assertEquals("400px", SystemPropertyUtils.getDefaultGridHeight());
        assertEquals("300px", SystemPropertyUtils.getDefaultSearchDialogGridHeight());
        assertEquals(2000, SystemPropertyUtils.getDefaultMessageDisplayTime().intValue());

        assertEquals("200px", SystemPropertyUtils.getDefaultTextAreaHeight());

        assertEquals(3, SystemPropertyUtils.getLookupFieldMaxItems());

        assertEquals("1200px", SystemPropertyUtils.getMinimumTwoColumnWidth());
        assertTrue(SystemPropertyUtils.isCapitalizeWords());

        assertTrue(SystemPropertyUtils.useDefaultPromptValue());
    }
}
