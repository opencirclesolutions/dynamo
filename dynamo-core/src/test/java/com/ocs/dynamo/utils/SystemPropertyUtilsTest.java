package com.ocs.dynamo.utils;

import org.junit.Assert;
import org.junit.Test;

public class SystemPropertyUtilsTest {

	@Test
	public void testDefaults() {
		Assert.assertEquals("€", SystemPropertyUtils.getDefaultCurrencySymbol());
		Assert.assertEquals("dd-MM-yyyy", SystemPropertyUtils.getDefaultDateFormat());
		Assert.assertEquals("dd-MM-yyyy HH:mm:ss", SystemPropertyUtils.getDefaultDateTimeFormat());
		Assert.assertEquals(2, SystemPropertyUtils.getDefaultDecimalPrecision());
		Assert.assertEquals(3, SystemPropertyUtils.getDefaultListSelectRows());
		Assert.assertEquals("HH:mm:ss", SystemPropertyUtils.getDefaultTimeFormat());

		Assert.assertEquals(false, SystemPropertyUtils.allowTableExport());
		Assert.assertEquals("\"", SystemPropertyUtils.getExportCsvQuoteChar());
		Assert.assertEquals(";", SystemPropertyUtils.getExportCsvSeparator());

		Assert.assertEquals(15000, SystemPropertyUtils.getMaximumExportRowsNonStreaming());
		Assert.assertEquals(10000, SystemPropertyUtils.getMaximumExportRowsStreaming());
		Assert.assertEquals(30000, SystemPropertyUtils.getMaximumExportRowsStreamingPivot());

		Assert.assertEquals(false, SystemPropertyUtils.useThousandsGroupingInEditMode());

		Assert.assertEquals("de", SystemPropertyUtils.getDefaultLocale());
	}
}
