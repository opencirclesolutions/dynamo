package com.ocs.dynamo.utils;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.util.SystemPropertyUtils;

public class SystemPropertyUtilsTest {

    @Test
    public void testDefaults() {
        Assert.assertEquals("€", SystemPropertyUtils.getDefaultCurrencySymbol());
        Assert.assertEquals("dd-MM-yyyy", SystemPropertyUtils.getDefaultDateFormat());
        Assert.assertEquals("dd-MM-yyyy HH:mm:ss", SystemPropertyUtils.getDefaultDateTimeFormat());
        Assert.assertEquals(2, SystemPropertyUtils.getDefaultDecimalPrecision());
        Assert.assertEquals("HH:mm:ss", SystemPropertyUtils.getDefaultTimeFormat());

        Assert.assertEquals(false, SystemPropertyUtils.allowListExport());
        Assert.assertEquals("\"", SystemPropertyUtils.getCsvQuoteChar());
        Assert.assertEquals(";", SystemPropertyUtils.getCsvSeparator());
        Assert.assertEquals("\"\"", SystemPropertyUtils.getCsvEscapeChar());
        
        Assert.assertEquals(false, SystemPropertyUtils.useThousandsGroupingInEditMode());

        Assert.assertEquals("en_GB", SystemPropertyUtils.getDefaultLocale());

        Assert.assertFalse(SystemPropertyUtils.getDefaultSearchCaseSensitive());
        Assert.assertFalse(SystemPropertyUtils.getDefaultSearchPrefixOnly());

        Assert.assertEquals("true", SystemPropertyUtils.getDefaultTrueRepresentation());
        Assert.assertEquals("false", SystemPropertyUtils.getDefaultFalseRepresentation());
        
        Assert.assertEquals("400px", SystemPropertyUtils.getDefaultGridHeight());
        Assert.assertEquals("300px", SystemPropertyUtils.getDefaultSearchDialogGridHeight());
        Assert.assertEquals(2000, SystemPropertyUtils.getDefaultMessageDisplayTime().intValue());
        
        Assert.assertEquals("200px", SystemPropertyUtils.getDefaultTextAreaHeight());
        
        Assert.assertEquals(3,  SystemPropertyUtils.getLookupFieldMaxItems());
        
        Assert.assertEquals("1200px", SystemPropertyUtils.getMinimumTwoColumnWidth());
        Assert.assertTrue(SystemPropertyUtils.isCapitalizeWords());
        
        Assert.assertTrue(SystemPropertyUtils.useDefaultPromptValue());
    }
}
