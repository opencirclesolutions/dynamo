package com.ocs.dynamo.ui.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IgnoreDiacriticCaptionFilterTest {

    @Test
    public void testIgnoreCaseContains() {
        IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(true, false);

        assertTrue(filter.test("Albert", "ber"));
        assertTrue(filter.test("Albërt", "ber"));
        assertTrue(filter.test("Albërt", "alb"));
        assertTrue(filter.test("albërt", "Alb"));
    }

    @Test
    public void testIgnoreCaseStartsWith() {
        IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(true, true);

        assertFalse(filter.test("Albert", "ber"));
        assertFalse(filter.test("Albërt", "ber"));
        assertTrue(filter.test("Albërt", "alb"));
        assertTrue(filter.test("albërt", "Alb"));
    }

    @Test
    public void testMatchCaseContains() {
        IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(false, false);

        assertTrue(filter.test("Albert", "ber"));
        assertTrue(filter.test("Albërt", "ber"));
        assertFalse(filter.test("Albërt", "alb"));
        assertFalse(filter.test("albërt", "Alb"));
    }

    @Test
    public void testMatchCaseStartsWith() {
        IgnoreDiacriticsCaptionFilter<String> filter = new IgnoreDiacriticsCaptionFilter<>(false, true);

        assertTrue(filter.test("Albert", "Alb"));
        assertFalse(filter.test("albert", "Alb"));
        assertFalse(filter.test("Albert", "alb"));
        assertFalse(filter.test("Albert", "bert"));

    }

    @Test
    public void testEquals() {

        IgnoreDiacriticsCaptionFilter<String> filter1 = new IgnoreDiacriticsCaptionFilter<>(false, false);
        assertFalse(filter1.equals(null));
        assertFalse(filter1.equals(new Object()));

        IgnoreDiacriticsCaptionFilter<String> filter2 = new IgnoreDiacriticsCaptionFilter<>(false, false);
        assertTrue(filter1.equals(filter2));

        IgnoreDiacriticsCaptionFilter<String> filter3 = new IgnoreDiacriticsCaptionFilter<>(false, true);
        assertFalse(filter1.equals(filter3));
    }
}
