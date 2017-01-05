/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.jasperreports.chart;

import java.util.Collection;

import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;

/**
 * Chart specific implementation of extra features
 *
 * @param <T>
 *            the specific plot implementation for the chart
 */
public interface CustomChartCustomizer<T extends Plot> {
	void setLabels(T plot);

	void addRangeMarkerToPlot(T plot, Marker marker);

	void addDomainMarkerToPlot(T plot, Marker marker);

	void addQuadrant(T plot, ChartCustomizer.Quadrant quadrant);

	void setStrokeTypes(T plot, Collection<ChartCustomizer.StrokeType> sts);

	void addAnnotationToPlot(T plot, Annotation annotation);
}
