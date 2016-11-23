package com.ocs.jasperreports.chart;

import java.util.Collection;

import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;

public interface CustomChartCustomizer<T extends Plot> {
	void setLabels(T plot);

	void addRangeMarkerToPlot(T plot, Marker marker);

	void addDomainMarkerToPlot(T plot, Marker marker);

	void addQuadrant(T plot, ChartCustomizer.Quadrant quadrant);

	void setStrokeTypes(T plot, Collection<ChartCustomizer.StrokeType> sts);

	void addAnnotationToPlot(T plot, Annotation annotation);
}
