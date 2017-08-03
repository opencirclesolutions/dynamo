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

import java.awt.Paint;
import java.util.Arrays;
import java.util.Collection;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;

import net.sf.jasperreports.components.charts.AbstractChartCustomizer;
import net.sf.jasperreports.components.charts.ChartComponent;
import net.sf.jasperreports.engine.JRRuntimeException;

/**
 * Chart customizer that adds several enhancements to spidercharts: customize legend and series colors.
 *
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
public class ChartComponentCustomizer extends AbstractChartCustomizer {

	private static final String SERIES_PAINT = ".SeriesPaint";

	/* (non-Javadoc)
	 * @see net.sf.jasperreports.components.charts.ChartCustomizer#customize(org.jfree.chart.JFreeChart, net.sf.jasperreports.components.charts.ChartComponent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void customize(JFreeChart chart, ChartComponent chartComponent) {
		String key = chartComponent.getContext().getComponentElement().getKey();

		// Adjust the legend in the chart when defined for this chart
		ChartCustomizer.customizeLegend(chart, getVariableValue(key + ChartCustomizer.LEGEND));

		// Apply spider specific customization
		if (chart.getPlot() instanceof SpiderWebPlot) {
			SpiderWebPlot plot = (SpiderWebPlot) chart.getPlot();

			// Apply paint
			try {
				Object seriesPaint = getVariableValue(key + SERIES_PAINT);
				Collection<Paint> sps = null;
				if (seriesPaint instanceof Collection<?>) {
					sps = (Collection<Paint>) seriesPaint;
				} else if (seriesPaint instanceof Paint[]) {
					sps = Arrays.asList((Paint[]) seriesPaint);
				}
				if (sps != null) {
					int i = 0;
					for (Paint p : sps) {
						plot.setSeriesPaint(i++, p);
					}
				} else if (seriesPaint instanceof Paint) {
					plot.setSeriesPaint((Paint) seriesPaint);
				}
			} catch (JRRuntimeException e) {
				// No markers defined and needed
			}
		}
	}

}
