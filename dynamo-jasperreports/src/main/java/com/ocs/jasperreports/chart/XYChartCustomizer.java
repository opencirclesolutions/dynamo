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
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Locale;

import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.BubbleXYItemLabelGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.Layer;

public class XYChartCustomizer extends AbstractChartCustomizer implements CustomChartCustomizer<XYPlot> {

	/**
	 * Define an instance (or collection of instances) of this class in a report variable with the
	 * name "re.Quadrant" (where re should be replaced with the name of the reporting element) to
	 * draw an annotation in the graph. Be sure to configure the applicable
	 * XXXChartRenderableDecorator (in jasper.properties).
	 */
	@SuppressWarnings("serial")
	public static class TextAnnotation extends XYTextAnnotation {

		public TextAnnotation(String text, double x, double y) {
			super(text, x, y);
		}

		public TextAnnotation(String text, double x, double y, String url) {
			super(text, x, y);
			setURL(url);
		}

		public TextAnnotation(String text, double x, double y, String url, String tooltip) {
			super(text, x, y);
			setURL(url);
			setToolTipText(tooltip);
		}

	}

	@Override
	public void setLabels(XYPlot plot) {
		plot.getRenderer().setBaseItemLabelGenerator(new XYChartCustomizer.XYLabelGenerator());
		plot.getRenderer().setBaseItemLabelsVisible(true);
	}

	@Override
	public void addRangeMarkerToPlot(XYPlot plot, Marker marker) {
		final ChartCustomizer.XYMarker rangeMarker = (ChartCustomizer.XYMarker) marker;
		final ValueAxis rangeAxis = plot.getRangeAxis();

		expandRange(rangeMarker, rangeAxis);

		plot.addRangeMarker(marker, Layer.FOREGROUND);
	}

	@Override
	public void addDomainMarkerToPlot(XYPlot plot, Marker marker) {
		final ChartCustomizer.XYMarker rangeMarker = (ChartCustomizer.XYMarker) marker;
		final ValueAxis domainAxis = plot.getDomainAxis();

		expandRange(rangeMarker, domainAxis);

		plot.addDomainMarker(marker, Layer.FOREGROUND);
	}

	@Override
	public void addQuadrant(XYPlot plot, ChartCustomizer.Quadrant quadrant) {
		// Define the quadrant
		Point2D pointQuadOrigin = new Point2D.Double(quadrant.getqOx(), quadrant.getqOy());
		plot.setQuadrantOrigin(pointQuadOrigin);

		// Set the colors
		plot.setQuadrantPaint(0, quadrant.getqClt());
		plot.setQuadrantPaint(1, quadrant.getqCrt());
		plot.setQuadrantPaint(2, quadrant.getqClb());
		plot.setQuadrantPaint(3, quadrant.getqCrb());

		// Set the annotations when needed
		if (quadrant.getUrlMessageFormat() != null || quadrant.getTooltipMessageFormat() != null) {
			// Determine extremes
			ValueAxis xAxis = plot.getDomainAxis();
			ValueAxis yAxis = plot.getRangeAxis();

			// Create LT annotation
			addXYBoxAnnotation(plot, xAxis.getLowerBound(), quadrant.getqOy(), quadrant.getqOx(),
			        yAxis.getUpperBound(), quadrant.getUrlMessageFormat(), quadrant.getTooltipMessageFormat());
			// Create RT annotation
			addXYBoxAnnotation(plot, quadrant.getqOx(), quadrant.getqOy(), xAxis.getUpperBound(),
			        yAxis.getUpperBound(), quadrant.getUrlMessageFormat(), quadrant.getTooltipMessageFormat());
			// Create LB annotation
			addXYBoxAnnotation(plot, xAxis.getLowerBound(), yAxis.getLowerBound(), quadrant.getqOx(),
			        quadrant.getqOy(), quadrant.getUrlMessageFormat(), quadrant.getTooltipMessageFormat());
			// Create RB annotation
			addXYBoxAnnotation(plot, quadrant.getqOx(), yAxis.getLowerBound(), xAxis.getUpperBound(),
			        quadrant.getqOy(), quadrant.getUrlMessageFormat(), quadrant.getTooltipMessageFormat());
		}
	}

	public void addXYBoxAnnotation(XYPlot plot, double minx, double miny, double maxx, double maxy,
	        String urlMessageFormat, String tooltipMessageFormat) {
		addXYBoxAnnotation(plot, minx, miny, maxx, maxy, null, null, urlMessageFormat, tooltipMessageFormat);
	}

	public void addXYBoxAnnotation(XYPlot plot, double minx, double miny, double maxx, double maxy, Stroke stroke,
	        Paint paint, String urlMessageFormat, String tooltipMessageFormat) {
		// Does the annotation area fit to visible area?
		if (minx < maxx && miny < maxy) {
			// Create LT annotation
			XYBoxAnnotation annotation = new XYBoxAnnotation(minx, miny, maxx, maxy, stroke, paint, null);
			if (urlMessageFormat != null) {
				MessageFormat mf = new MessageFormat(urlMessageFormat, Locale.ENGLISH);
				annotation.setURL(mf.format(new Object[] { minx, miny, maxx, maxy }));
			}
			if (tooltipMessageFormat != null) {
				annotation.setToolTipText(MessageFormat.format(tooltipMessageFormat, minx, miny, maxx, maxy));
			}
			// Add annotation
			plot.getRenderer().addAnnotation(annotation, Layer.BACKGROUND);
		}
	}

	@Override
	public void setStrokeTypes(XYPlot plot, Collection<ChartCustomizer.StrokeType> sts) {
		// TODO not implemented yet
	}

	@SuppressWarnings("serial")
	private static class XYLabelGenerator extends BubbleXYItemLabelGenerator {
		@Override
		public String generateLabel(XYDataset dataset, int series, int item) {
			if (dataset instanceof XYZDataset) {
				Number z = ((XYZDataset) dataset).getZ(series, item);
				if (z != null && z instanceof ChartCustomizer.BigDecimalLabelWrapper) {
					return ((ChartCustomizer.BigDecimalLabelWrapper) z).getLabel();
				}
			}
			return String.valueOf(dataset.getSeriesKey(series));
		}
	}

	@Override
	public void addAnnotationToPlot(XYPlot plot, Annotation annotation) {
		if (annotation instanceof XYAnnotation) {
			plot.addAnnotation((XYAnnotation) annotation);
		}
	}

}
