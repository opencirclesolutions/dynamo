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

import java.awt.Stroke;
import java.util.Collection;

import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.Layer;

public class CategoryChartCustomizer extends AbstractChartCustomizer implements CustomChartCustomizer<CategoryPlot> {

	@Override
	public void setLabels(CategoryPlot plot) {
		plot.getRenderer().setBaseItemLabelGenerator(new CategoryLabelGenerator());
		plot.getRenderer().setBaseItemLabelsVisible(true);
	}

	@Override
	public void addRangeMarkerToPlot(CategoryPlot plot, Marker marker) {
		final ChartCustomizer.XYMarker rangeMarker = (ChartCustomizer.XYMarker) marker;
		final ValueAxis rangeAxis = plot.getRangeAxis();

		expandRange(rangeMarker, rangeAxis);

		plot.addRangeMarker(marker, Layer.FOREGROUND);
	}

	@Override
	public void addDomainMarkerToPlot(CategoryPlot plot, Marker marker) {
		final CategoryAxis domainAxis = plot.getDomainAxis();
		if (!(domainAxis instanceof FullWidthCategoryDomainAxis)) {
			final FullWidthCategoryDomainAxis categoryColoredDomainAxis = new FullWidthCategoryDomainAxis(domainAxis);

			plot.setDomainAxis(categoryColoredDomainAxis);
		}

		plot.addDomainMarker((CategoryMarker) marker, Layer.BACKGROUND);
	}

	@Override
	public void addQuadrant(CategoryPlot plot, ChartCustomizer.Quadrant quadrant) {
		throw new UnsupportedOperationException("addQuadrant is unsupported for Category charts");
	}

	@Override
	public void setStrokeTypes(CategoryPlot plot, Collection<ChartCustomizer.StrokeType> sts) {
		for (ChartCustomizer.StrokeType st : sts) {
			final int seriesIndex = st.getSeriesIndex();
			final Stroke stroke = st.getStroke();

			final int datasetCount = plot.getDatasetCount();
			int totalSeries = 0;
			for (int i = 0; i < datasetCount; i++) {
				final CategoryDataset dataset = plot.getDataset(i);
				final int rowCount = dataset.getRowCount();

				// selected serie is in this dataset (multi axis chart can have multiple datasets)
				if (totalSeries + rowCount >= seriesIndex) {
					// dataset and renders are synchronized in counting (?)
					plot.getRenderer(i).setSeriesStroke(seriesIndex - totalSeries - 1, stroke);
				} else {
					totalSeries += rowCount;
				}
			}
		}
	}

	@SuppressWarnings("serial")
	static class CategoryLabelGenerator extends StandardCategoryItemLabelGenerator {
		@Override
		public String generateLabel(CategoryDataset dataset, int row, int column) {
			final Number value = dataset.getValue(row, column);
			if (value != null && value instanceof ChartCustomizer.BigDecimalLabelWrapper) {
				return ((ChartCustomizer.BigDecimalLabelWrapper) value).getLabel();
			}

			return super.generateLabel(dataset, row, column);
		}
	}

	@Override
	public void addAnnotationToPlot(CategoryPlot plot, Annotation annotation) {
		// TODO not implemented yet
	}

}