package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

public class IgnoreDiacriticCaptionFilterTest {

	@Test
	public void testIgnoreCaseContains() {
		IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(true, false);

		Assert.assertTrue(filter.test("Albert", "ber"));
		Assert.assertTrue(filter.test("Albërt", "ber"));
		Assert.assertTrue(filter.test("Albërt", "alb"));
		Assert.assertTrue(filter.test("albërt", "Alb"));
	}

	@Test
	public void testIgnoreCaseStartsWith() {
		IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(true, true);

		Assert.assertFalse(filter.test("Albert", "ber"));
		Assert.assertFalse(filter.test("Albërt", "ber"));
		Assert.assertTrue(filter.test("Albërt", "alb"));
		Assert.assertTrue(filter.test("albërt", "Alb"));
	}

	@Test
	public void testMatchCaseContains() {
		IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(false, false);

		Assert.assertTrue(filter.test("Albert", "ber"));
		Assert.assertTrue(filter.test("Albërt", "ber"));
		Assert.assertFalse(filter.test("Albërt", "alb"));
		Assert.assertFalse(filter.test("albërt", "Alb"));
	}

	@Test
	public void testMatchCaseStartsWith() {
		IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(false, true);

		Assert.assertTrue(filter.test("Albert", "Alb"));
		Assert.assertFalse(filter.test("albert", "Alb"));
		Assert.assertFalse(filter.test("Albert", "alb"));
		Assert.assertFalse(filter.test("Albert", "bert"));

	}

	@Test
	public void testEquals() {

		IgnoreDiacriticsCaptionFilter<String> filter1 = new IgnoreDiacriticsCaptionFilter<>(false, false);
		Assert.assertFalse(filter1.equals(null));
		Assert.assertFalse(filter1.equals(new Object()));

		IgnoreDiacriticsCaptionFilter<String> filter2 = new IgnoreDiacriticsCaptionFilter<>(false, false);
		Assert.assertTrue(filter1.equals(filter2));

		IgnoreDiacriticsCaptionFilter<String> filter3 = new IgnoreDiacriticsCaptionFilter<>(false, true);
		Assert.assertFalse(filter1.equals(filter3));
	}
}
