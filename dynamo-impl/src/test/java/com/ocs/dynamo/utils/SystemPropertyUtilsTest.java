package com.ocs.dynamo.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.model.ThousandsGroupingMode;
import com.ocs.dynamo.util.SystemPropertyUtils;

import java.util.Locale;

public class SystemPropertyUtilsTest {

	@Test
	public void testDefaults() {


		assertEquals("€", SystemPropertyUtils.getDefaultCurrencySymbol());
		assertEquals("dd-MM-yyyy", SystemPropertyUtils.getDefaultDateFormat());
		assertEquals("dd-MM-yyyy HH:mm:ss", SystemPropertyUtils.getDefaultDateTimeFormat());
		assertEquals(2, SystemPropertyUtils.getDefaultDecimalPrecision());
		assertEquals("HH:mm:ss", SystemPropertyUtils.getDefaultTimeFormat());

		assertFalse(SystemPropertyUtils.allowListExport());
		assertEquals("\"", SystemPropertyUtils.getCsvQuoteChar());
		assertEquals(";", SystemPropertyUtils.getCsvSeparator());
		assertEquals("\"\"", SystemPropertyUtils.getCsvEscapeChar());

		assertEquals(ThousandsGroupingMode.ALWAYS, SystemPropertyUtils.getDefaultThousandsGroupingMode());

		Locale loc = new Locale.Builder().setLanguage("en").setRegion("GB").build();
		assertThat(SystemPropertyUtils.getDefaultLocale()).isEqualTo(loc);

		assertFalse(SystemPropertyUtils.getDefaultSearchCaseSensitive());
		assertFalse(SystemPropertyUtils.getDefaultSearchPrefixOnly());

		assertEquals("true", SystemPropertyUtils.getDefaultTrueRepresentation());
		assertEquals("false", SystemPropertyUtils.getDefaultFalseRepresentation());

		assertEquals("400px", SystemPropertyUtils.getDefaultGridHeight());
		assertEquals("300px", SystemPropertyUtils.getDefaultSearchDialogGridHeight());
		assertEquals(2000, SystemPropertyUtils.getDefaultMessageDisplayTime().intValue());

		assertEquals("200px", SystemPropertyUtils.getDefaultTextAreaHeight());

		assertEquals(3, SystemPropertyUtils.getDefaultLookupFieldMaxItems());
		assertTrue(SystemPropertyUtils.isCapitalizeWords());
		assertTrue(SystemPropertyUtils.useDefaultPromptValue());
	}
}
