package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

public class IgnoreDiacriticCaptionFilterTest {

	@Test
	public void testIgnoreCaseContains() {
		IgnoreDiacriticsCaptionFilter filter = new IgnoreDiacriticsCaptionFilter(true, false);

		Assert.assertTrue(filter.test("Albert", "ber"));
		Assert.assertTrue(filter.test("Albërt", "ber"));
		Assert.assertTrue(filter.test("Albërt", "alb"));
		Assert.assertTrue(filter.test("albërt", "Alb"));
	}

	@Test
	public void testIgnoreCaseStartsWith() {
		IgnoreDiacriticsCaptionFilter filter = new IgnoreDiacriticsCaptionFilter(true, true);

		Assert.assertFalse(filter.test("Albert", "ber"));
		Assert.assertFalse(filter.test("Albërt", "ber"));
		Assert.assertTrue(filter.test("Albërt", "alb"));
		Assert.assertTrue(filter.test("albërt", "Alb"));
	}

	@Test
	public void testMatchCaseContains() {
		IgnoreDiacriticsCaptionFilter filter = new IgnoreDiacriticsCaptionFilter(false, false);

		Assert.assertTrue(filter.test("Albert", "ber"));
		Assert.assertTrue(filter.test("Albërt", "ber"));
		Assert.assertFalse(filter.test("Albërt", "alb"));
		Assert.assertFalse(filter.test("albërt", "Alb"));
	}

	@Test
	public void testMatchCaseStartsWith() {
		IgnoreDiacriticsCaptionFilter filter = new IgnoreDiacriticsCaptionFilter(false, true);

		Assert.assertTrue(filter.test("Albert", "Alb"));
		Assert.assertFalse(filter.test("albert", "Alb"));
		Assert.assertFalse(filter.test("Albert", "alb"));
		Assert.assertFalse(filter.test("Albert", "bert"));

	}
}
