package com.ocs.dynamo.ui.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MultiSelectIgnoreDiacriticsCaptionFilterTest {

	@Test
	public void testIgnoreCaseContains() {
		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				true, false);

		assertTrue(filter.test("Albert", "ber"));
		assertTrue(filter.test("Albërt", "ber"));
		assertTrue(filter.test("Albërt", "alb"));
		assertTrue(filter.test("albërt", "Alb"));
	}

	@Test
	public void testIgnoreCaseStartsWith() {
		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				true, true);

		assertFalse(filter.test("Albert", "ber"));
		assertFalse(filter.test("Albërt", "ber"));
		assertTrue(filter.test("Albërt", "alb"));
		assertTrue(filter.test("albërt", "Alb"));
	}

	@Test
	public void testMatchCaseContains() {
		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				false, false);

		assertTrue(filter.test("Albert", "ber"));
		assertTrue(filter.test("Albërt", "ber"));
		assertFalse(filter.test("Albërt", "alb"));
		assertFalse(filter.test("albërt", "Alb"));
	}

	@Test
	public void testMatchCaseStartsWith() {
		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				false, true);

		assertTrue(filter.test("Albert", "Alb"));
		assertFalse(filter.test("albert", "Alb"));
		assertFalse(filter.test("Albert", "alb"));
		assertFalse(filter.test("Albert", "bert"));
	}

	@Test
	public void testEquals() {

		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter1 = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				false, false);
		assertNotEquals(null, filter1);
		assertNotEquals(filter1, new Object());

		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter2 = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				false, false);
		assertEquals(filter1, filter2);

		MultiSelectIgnoreDiacriticsCaptionFilter<String> filter3 = new MultiSelectIgnoreDiacriticsCaptionFilter<>(null,
				false, true);
		assertNotEquals(filter1, filter3);
	}
}
