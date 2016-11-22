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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.VerticalAlignment;

import net.sf.jasperreports.engine.JRAbstractChartCustomizer;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRRuntimeException;

/**
 * Chart customizer that adds several enhancements to PIE charts
 *
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
public class PieChartCustomizer extends JRAbstractChartCustomizer {

	private static final String LEGEND = ".Legend";

	/**
	 * Class to override settings of the legend
	 */
	public static class LegendOptions {
		private Integer border;
		private HorizontalAlignment horizontalAlignment;
		private VerticalAlignment verticalAlignment;

		public LegendOptions(Integer border, HorizontalAlignment horizontalAlignment,
				VerticalAlignment verticalAlignment) {
			super();
			this.border = border;
			this.horizontalAlignment = horizontalAlignment;
			this.verticalAlignment = verticalAlignment;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jasperreports.engine.JRChartCustomizer#customize(org.jfree.chart.JFreeChart,
	 * net.sf.jasperreports.engine.JRChart)
	 */
	@Override
	public void customize(JFreeChart freeChart, JRChart chart) {
		String key = chart.getKey();

		// Adjust the legend in the chart when defined for this chart
		try {
			Object v = getVariableValue(key + LEGEND);
			if (v instanceof LegendTitle) {
				freeChart.removeLegend();
				freeChart.addLegend((LegendTitle) v);
			}
			if (v instanceof LegendOptions) {
				LegendOptions lo = (LegendOptions) v;
				LegendTitle legend = freeChart.getLegend();
				if (lo.border != null) {
					legend.setBorder(lo.border, lo.border, lo.border, lo.border);
				}
				if (lo.horizontalAlignment != null) {
					legend.setHorizontalAlignment(lo.horizontalAlignment);
				}
				if (lo.verticalAlignment != null) {
					legend.setVerticalAlignment(lo.verticalAlignment);
				}
			}
		} catch (JRRuntimeException e) {
			// No legend defined and needed
		}

	}

}
