package com.ocs.dynamo.ui.component;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;

public class DateTimePickerTest extends BaseMockitoTest {

    private Locale locale = new Locale("nl");

    @BeforeEach
    public void before() {
        MockVaadin.setup();
    }

    @Test
    public void testSetValue() {
        DateTimePicker picker = new DateTimePicker(locale);

        assertNull(picker.getValue());

        picker.setValue(LocalDateTime.of(2019, 4, 3, 10, 12));

        DatePicker dp = picker.getDatePicker();
        assertEquals(LocalDate.of(2019, 4, 3), dp.getValue());

        TimePicker tp = picker.getTimePicker();
        assertEquals(LocalTime.of(10, 12), tp.getValue());
    }

    /**
     * Verify that default time 00:00 is used when not specified
     */
    @Test
    public void testGetValueDefaultTime() {
        DateTimePicker picker = new DateTimePicker(locale);

        assertNull(picker.generateModelValue());

        DatePicker dp = picker.getDatePicker();
        dp.setValue(LocalDate.of(2019, 11, 11));

        assertEquals(LocalDateTime.of(2019, 11, 11, 0, 0), picker.generateModelValue());

        TimePicker tp = picker.getTimePicker();
        tp.setValue(LocalTime.of(13, 12));

        assertEquals(LocalDateTime.of(2019, 11, 11, 13, 12), picker.generateModelValue());
    }

    @Test
    public void testClear() {
        DateTimePicker picker = new DateTimePicker(locale);

        picker.setValue(LocalDateTime.of(2019, 4, 3, 10, 12));
        picker.clear();

        DatePicker dp = picker.getDatePicker();
        TimePicker tp = picker.getTimePicker();

        assertNull(picker.generateModelValue());
        assertNull(dp.getValue());
        assertNull(tp.getValue());
    }
}
