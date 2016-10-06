package com.ocs.jasperreports.chart;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;

public class AbstractChartCustomizer {
	protected void expandRange(ChartCustomizer.XYMarker rangeMarker, ValueAxis rangeAxis) {
		if (!rangeMarker.isExpandAxis()) {
			return;
		}

		final double rangeMarkerValue = rangeMarker.getValue();

		// always 15% extra to show text
		final double extraSpace = rangeMarkerValue / 100 * 15;
		final double rangeMarkerUpper = rangeMarkerValue + extraSpace;
		final double rangeMarkerLower = rangeMarkerValue - extraSpace;

		final Range axisRange = rangeAxis.getRange();

		// if it does not fit in range, expand
		if (rangeMarkerLower < axisRange.getLowerBound()) {
			setLowerBound(rangeMarkerLower, rangeAxis);
		}

		if (rangeMarkerValue > axisRange.getUpperBound()) {
			setUpperBound(rangeMarkerUpper, rangeAxis);
		}
	}

	private void setLowerBound(double min, ValueAxis rangeAxis) {
		if (rangeAxis.getUpperBound() > min) {
			rangeAxis.setRange(new Range(min, rangeAxis.getUpperBound()), true, false);
		} else {
			rangeAxis.setRange(new Range(min, min + 1.0), true, false);
		}
	}

	private void setUpperBound(double max, ValueAxis rangeAxis) {
		if (rangeAxis.getLowerBound() < max) {
			rangeAxis.setRange(new Range(rangeAxis.getLowerBound(), max), true, false);
		} else {
			rangeAxis.setRange(new Range(max - 1.0, max), true, false);
		}
	}
}
