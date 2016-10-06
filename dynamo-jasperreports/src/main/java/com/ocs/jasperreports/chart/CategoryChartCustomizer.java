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

class CategoryChartCustomizer extends AbstractChartCustomizer implements CustomChartCustomizer<CategoryPlot> {

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