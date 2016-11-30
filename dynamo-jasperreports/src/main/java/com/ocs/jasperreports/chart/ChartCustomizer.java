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

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.sf.jasperreports.charts.util.ChartHyperlinkProvider;
import net.sf.jasperreports.charts.util.SvgChartRendererFactory;
import net.sf.jasperreports.engine.JRAbstractChartCustomizer;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.base.JRBasePrintHyperlink;
import net.sf.jasperreports.engine.type.HyperlinkTypeEnum;
import net.sf.jasperreports.renderers.Renderable;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chart customizer that adds several enhancements to charts: draw labels, quadrants and markers.
 *
 * @author Patrick Deenen (patrick@opencircle.solutions)
 */
public class ChartCustomizer<T extends Plot> extends JRAbstractChartCustomizer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChartCustomizer.class);

	private static final String ANNOTATION = ".Annotation";
	private static final String LABELS = ".Labels";
	private static final String MARKER_RANGE = ".MarkerRange";
	private static final String MARKER_DOMAIN = ".MarkerDomain";
	private static final String QUADRANT = ".Quadrant";
	private static final String STROKE_TYPE = ".StrokeType";
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

	@SuppressWarnings("serial")
	public static class ChartHyperlinkProviderDecorator implements ChartHyperlinkProvider {

		private ChartHyperlinkProvider chartHyperlinkProvider;

		public ChartHyperlinkProviderDecorator(ChartHyperlinkProvider chartHyperlinkProvider) {
			super();
			this.chartHyperlinkProvider = chartHyperlinkProvider;
		}

		@Override
		public JRPrintHyperlink getEntityHyperlink(ChartEntity entity) {
			if (entity instanceof XYAnnotationEntity) {
				XYAnnotationEntity e = (XYAnnotationEntity) entity;
				JRBasePrintHyperlink link = new JRBasePrintHyperlink();
				link.setHyperlinkType(HyperlinkTypeEnum.REFERENCE);
				link.setHyperlinkReference(e.getURLText());
				link.setHyperlinkTooltip(e.getToolTipText());
				return link;
			}
			return chartHyperlinkProvider.getEntityHyperlink(entity);
		}

		@Override
		public boolean hasHyperlinks() {
			return chartHyperlinkProvider.hasHyperlinks();
		}

	}

	public static class SvgChartRenderableFactoryDecorator extends SvgChartRendererFactory {

		@Override
		public Renderable getRenderable(JasperReportsContext jasperReportsContext, JFreeChart chart,
				ChartHyperlinkProvider chartHyperlinkProvider, Rectangle2D rectangle) {
			return super.getRenderable(jasperReportsContext, chart,
					new ChartHyperlinkProviderDecorator(chartHyperlinkProvider), rectangle);
		}

	}

	/**
	 * Use this class to add support for labels for individual rows in charts which only support
	 * this for the series key, for example the Bubble chart only support series labels.
	 */
	@SuppressWarnings("serial")
	public static class BigDecimalLabelWrapper extends BigDecimal {

		private String label;

		/**
		 * Default constructor
		 *
		 * @param value The actual BigDecimal
		 * @param label
		 */
		public BigDecimalLabelWrapper(BigDecimal value, String label) {
			super(value.doubleValue());
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	}

	public static enum Action {
		ZOOM
	}

	/**
	 * Define an instance (or collection of instances) of this class in a report variable with the name "re.Quadrant"
	 * (where re should be replaced with the name of the reporting element) to draw a quadrant in the graph.
	 */
	public static class Quadrant {

		private double qOx;
		private double qOy;
		private Color qClt;
		private Color qCrt;
		private Color qClb;
		private Color qCrb;
		private String urlMessageFormat;
		private String tooltipMessageFormat;

		/**
		 * Define a quadrant with applicable colors for each quadrent.
		 * 
		 * @param qOx
		 * @param qOy
		 * @param qClt
		 * @param qCrt
		 * @param qClb
		 * @param qCrb
		 */
		public Quadrant(double qOx, double qOy, Color qClt, Color qCrt, Color qClb, Color qCrb) {
			super();
			this.qOx = qOx;
			this.qOy = qOy;
			this.qClt = qClt;
			this.qCrt = qCrt;
			this.qClb = qClb;
			this.qCrb = qCrb;
		}

		/**
		 * Define a quadrant with applicable colors for each quadrant. When action is ZOOM then each visible quadrant
		 * can be selected. The url and tooltip will be assigned to each quadrant using 4 parameters: 0=min x, 1=min y,
		 * 2=max x, 3=max y.
		 * 
		 * @param qOx
		 * @param qOy
		 * @param qClt
		 * @param qCrt
		 * @param qClb
		 * @param qCrb
		 * @param urlMessageFormat
		 * @param tooltipMessageFormat
		 */
		public Quadrant(double qOx, double qOy, Color qClt, Color qCrt, Color qClb, Color qCrb,
				String urlMessageFormat, String tooltipMessageFormat) {
			super();
			this.qOx = qOx;
			this.qOy = qOy;
			this.qClt = qClt;
			this.qCrt = qCrt;
			this.qClb = qClb;
			this.qCrb = qCrb;
			if (urlMessageFormat != null) {
				this.urlMessageFormat = urlMessageFormat;
			}
			if (tooltipMessageFormat != null) {
				this.tooltipMessageFormat = tooltipMessageFormat;
			}
		}

		public double getqOx() {
			return qOx;
		}

		public double getqOy() {
			return qOy;
		}

		public Color getqClt() {
			return qClt;
		}

		public Color getqCrt() {
			return qCrt;
		}

		public Color getqClb() {
			return qClb;
		}

		public Color getqCrb() {
			return qCrb;
		}

		public String getUrlMessageFormat() {
			return urlMessageFormat;
		}

		public String getTooltipMessageFormat() {
			return tooltipMessageFormat;
		}
	}

	/**
	 * Define an instance of this class in a report variable with the name "re.Marker[Range|Domain]"
	 * (where re should be replaced with the name of the reporting element) to draw a marker in the
	 * graph. When "Range" is specified the marker will be drawn on the range axis otherwise the
	 * domain axis.
	 */
	@SuppressWarnings("serial")
	public static class XYMarker extends ValueMarker {
		private boolean expandAxis;

		public XYMarker(double value, Color color, String label, boolean expandAxis) {
			super(value);
			this.expandAxis = expandAxis;
			setPaint(color);
			setLabel(label);
		}

		public XYMarker(double value, Color color, String label, float alpha, Color labelColor, boolean expandAxis) {
			super(value);
			this.expandAxis = expandAxis;
			setPaint(color);
			setLabel(label);
			setAlpha(alpha);
			setLabelPaint(labelColor);
			setLabelBackgroundColor(Color.white);
		}

		public XYMarker(double value, Paint paint, Stroke stroke, Paint outlinePaint, Stroke outlineStroke, float alpha,
				boolean expandAxis) {
			super(value, paint, stroke, outlinePaint, outlineStroke, alpha);
			this.expandAxis = expandAxis;
		}

		public XYMarker(double value, Paint paint, Stroke stroke, boolean expandAxis) {
			super(value, paint, stroke);
			this.expandAxis = expandAxis;
		}

		public XYMarker(double value, boolean expandAxis) {
			super(value);
			this.expandAxis = expandAxis;
		}

		public boolean isExpandAxis() {
			return expandAxis;
		}
	}

	/**
	 * Define an instance of this class in a report variable with the name "re.MarkerDomain"
	 * (where re should be replaced with the name of the reporting element) to mark a category in the
	 * graph.
	 */
	@SuppressWarnings({ "serial", "rawtypes" })
	public static class CategoryMarker extends org.jfree.chart.plot.CategoryMarker {

		public CategoryMarker(Comparable key) {
			super(key);
		}

		public CategoryMarker(Comparable key, Paint paint, Stroke stroke) {
			super(key, paint, stroke);
		}

		public CategoryMarker(Comparable key, Paint paint, Stroke stroke, Paint outlinePaint,
							  Stroke outlineStroke, float alpha) {
			super(key, paint, stroke, outlinePaint, outlineStroke, alpha);
		}
	}

	public static class StrokeType {
		private final int seriesIndex;
		private final Stroke stroke;

		public StrokeType(int seriesIndex, Stroke stroke) {
			this.seriesIndex = seriesIndex;
			this.stroke = stroke;
		}

		public int getSeriesIndex() {
			return seriesIndex;
		}

		public Stroke getStroke() {
			return stroke;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.jasperreports.engine.JRChartCustomizer#customize(org.jfree.chart.JFreeChart,
	 * net.sf.jasperreports.engine.JRChart)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void customize(JFreeChart chart, JRChart jasperChart) {
		final T plot = (T) chart.getPlot();
		String key = jasperChart.getKey();

		try {
			// Adjust the legend in the chart when defined for this chart
			Object v = getVariableValue(key + LEGEND);
			if (v instanceof LegendTitle) {
				chart.removeLegend();
				chart.addLegend((LegendTitle) v);
			}
			if (v instanceof LegendOptions) {
				LegendOptions lo = (LegendOptions) v;
				LegendTitle legend = chart.getLegend();
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

		// All customizations for which a plot customizer is needed
		CustomChartCustomizer chartCustomizer = null;

		if (plot instanceof XYPlot) {
			chartCustomizer = new XYChartCustomizer();
		} else if (plot instanceof CategoryPlot) {
			chartCustomizer = new CategoryChartCustomizer();
		} else {
			LOGGER.info("No implementation available to customize");
			return;
		}

		// Paint a quadrant in the chart when defined for this chart
		try {
			Quadrant q = (Quadrant) getVariableValue(key + QUADRANT);
			if (q != null) {
				chartCustomizer.addQuadrant(plot, q);
			}
		} catch (JRRuntimeException e) {
			// No quadrant defined and needed
		}

		try {
			// Paint labels for the rows when defined for this chart
			if (Boolean.TRUE.equals((Boolean) getVariableValue(key + LABELS))) {
				chartCustomizer.setLabels(plot);
			}
		} catch (JRRuntimeException e) {
			// No markers defined and needed
		}

		// Paint Range markers when defined for this chart
		try {
			Object markers = getVariableValue(key + MARKER_RANGE);
			addMarkers(chartCustomizer, plot, markers, true);
		} catch (JRRuntimeException e) {
			// No markers defined and needed
		}

		// Paint Domain markers when defined for this chart
		try {
			Object markers = getVariableValue(key + MARKER_DOMAIN);
			addMarkers(chartCustomizer, plot, markers, false);
		} catch (JRRuntimeException e) {
			// No markers defined and needed
		}

		try {
			Object strokeTypes = getVariableValue(key + STROKE_TYPE);
			addStrokeTypes(chartCustomizer, plot, strokeTypes);
		} catch (JRRuntimeException e) {
			// no stroke types defined
		}

		try {
			// Paint annotations for the rows when defined for this chart
			Object annotations = getVariableValue(key + ANNOTATION);
			if (annotations != null) {
				addAnnotations(chartCustomizer, plot, annotations);
			}
		} catch (JRRuntimeException e) {
			// No annotations defined and needed
		}

		if (chartCustomizer instanceof JRChartCustomizer) {
			// Delegate to custom implementation
			((JRChartCustomizer) chartCustomizer).customize(chart, jasperChart);
		}
	}

	@SuppressWarnings("unchecked")
	private void addStrokeTypes(CustomChartCustomizer<T> chartCustomizer, T plot, Object strokeTypes) {
		Collection<StrokeType> sts = Collections.emptyList();
		if (strokeTypes instanceof Collection<?>) {
			sts = (Collection<StrokeType>) strokeTypes;
		} else if (strokeTypes instanceof StrokeType[]) {
			sts = Arrays.asList((StrokeType[]) strokeTypes);
		} else if (strokeTypes instanceof StrokeType) {
			sts = Collections.singleton((StrokeType) strokeTypes);
		}

		chartCustomizer.setStrokeTypes(plot, sts);
	}

	@SuppressWarnings("unchecked")
	protected void addMarkers(CustomChartCustomizer<T> customChartCustomizer, T plot,
							  Object markers, boolean range) {
		Collection<Marker> mks = null;
		if (markers instanceof Collection<?>) {
			mks = (Collection<Marker>) markers;
		} else if (markers instanceof Marker[]) {
			mks = Arrays.asList((Marker[]) markers);
		}
		if (mks != null) {
			for (Marker m : mks) {
				addMarker(customChartCustomizer, plot, m, range);
			}
		} else if (markers instanceof Marker) {
			addMarker(customChartCustomizer, plot, (Marker) markers, range);
		}
	}

	protected void addMarker(CustomChartCustomizer<T> customChartCustomizer, T plot, Marker marker,
							 boolean range) {
		if (marker != null) {
			if (range) {
				marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);

				customChartCustomizer.addRangeMarkerToPlot(plot, marker);
			} else {
				marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
				marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

				customChartCustomizer.addDomainMarkerToPlot(plot, marker);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void addAnnotations(CustomChartCustomizer<T> customChartCustomizer, T plot, Object annotations) {
		Collection<Annotation> ans = null;
		if (annotations instanceof Collection<?>) {
			ans = (Collection<Annotation>) annotations;
		} else if (annotations instanceof Annotation[]) {
			ans = Arrays.asList((Annotation[]) annotations);
		}
		if (ans != null) {
			for (Annotation a : ans) {
				addAnnotation(customChartCustomizer, plot, a);
			}
		} else if (annotations instanceof Annotation) {
			addAnnotation(customChartCustomizer, plot, (Annotation) annotations);
		}
	}

	protected void addAnnotation(CustomChartCustomizer<T> customChartCustomizer, T plot, Annotation annotation) {
		if (annotation != null) {
			customChartCustomizer.addAnnotationToPlot(plot, annotation);
		}
	}
}
