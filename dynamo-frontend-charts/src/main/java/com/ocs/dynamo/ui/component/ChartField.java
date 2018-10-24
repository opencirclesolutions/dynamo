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
package com.ocs.dynamo.ui.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataLabels;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotOptionsBar;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;

/**
 * Custom field for displaying charts
 * 
 * @author patrickdeenen
 *
 */
public class ChartField<T> extends CustomField<Collection<T>> {

	private static final long serialVersionUID = 8761050876630094655L;

	private AttributeModel seriesAttribute;
	private AttributeModel nameAttribute;
	private AttributeModel dataAttribute;
	private XAxis xaxis;
	private YAxis yaxis;
	private ArrayList<Series> series;
	private Chart chart = new Chart();

	public ChartField() {
		super();
	}

	/**
	 * @param chart
	 */
	public ChartField(Chart chart) {
		super();
		this.chart = chart;
	}

	/**
	 * @return the chart
	 */
	public Chart getChart() {
		return chart;
	}

	/**
	 * @param chart
	 *            the chart to set
	 */
	public void setChart(Chart chart) {
		this.chart = chart;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<Collection<T>> getType() {
		return (Class<Collection<T>>) (Class<?>) Collection.class;
	}

	@Override
	protected Component initContent() {
		return chart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.AbstractField#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Collection<T> data) throws ReadOnlyException, ConversionException {
		super.setValue(data);
		if (chart != null && chart.getConfiguration() != null) {
			createSeries(data);
			chart.getConfiguration().setSeries(series);
			chart.drawChart();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
	 */
	@Override
	protected void setInternalValue(Collection<T> data) {
		super.setInternalValue(data);
		if (chart != null && chart.getConfiguration() != null) {
			createSeries(data);
			chart.getConfiguration().setSeries(series);
			chart.drawChart();
		}
	}

	/**
	 * Create the chart
	 * 
	 * @param chartType
	 * @param title
	 * @param subTitle
	 * @param tooltip
	 * @return
	 */
	Chart createChart(ChartType chartType, String title, String subTitle, String tooltip) {
		if (chartType != null) {
			chart.getConfiguration().getChart().setType(chartType);
		}

		Configuration conf = chart.getConfiguration();

		if (!StringUtils.isBlank(title)) {
			conf.setTitle(title);
		}
		if (!StringUtils.isBlank(subTitle)) {
			conf.setSubTitle(subTitle);
		}

		createAxis();
		conf.addxAxis(xaxis);
		conf.addyAxis(yaxis);

		if (!StringUtils.isBlank(tooltip)) {
			Tooltip tt = new Tooltip();
			tt.setFormatter(tooltip);
			conf.setTooltip(tt);
		}

		PlotOptionsBar plot = new PlotOptionsBar();
		plot.setDataLabels(new DataLabels(true));
		conf.setPlotOptions(plot);

		conf.setLegend(createLegend());
		conf.disableCredits();

		if (series != null) {
			conf.setSeries(series);
		}

		chart.drawChart(conf);
		return chart;
	}

	void createAxis() {
		if (xaxis == null && nameAttribute != null) {
			xaxis = new XAxis();
			xaxis.setTitle(nameAttribute.getDisplayName());
		}
		if (yaxis == null && seriesAttribute != null) {
			yaxis = new YAxis();
			yaxis.setTitle(seriesAttribute.getDisplayName());
		}
	}

	/**
	 * This method assumes the list with data is sorted by "series, name, y" and that all categories are available in
	 * the first series found. Also creates the x and y axis.
	 * 
	 * @return The created series
	 */
	ArrayList<Series> createSeries(Collection<T> source) {
		series = new ArrayList<>();
		if (source != null && !source.isEmpty() && nameAttribute != null && dataAttribute != null
				&& Number.class.isAssignableFrom(dataAttribute.getNormalizedType())) {
			createAxis();
			HashSet<String> categories = new HashSet<>();
			ListSeries currentSeries = new ListSeries();
			ArrayList<Number> data = new ArrayList<>();

			for (Object item : source) {

				// Handle series creation
				Object value = ClassUtils.getFieldValue(item,
						StringUtils.substringAfter(seriesAttribute.getPath(), "."));
				if (value != null) {
					if (currentSeries.getName() == null) {
						// Init first created series
						currentSeries.setName(value.toString());
					} else if (!currentSeries.getName().equals(value.toString())) {
						// Found new name, hence finish current series and starting creating new
						if (!data.isEmpty()) {
							currentSeries.setData(data);
							series.add(currentSeries);
						}
						currentSeries = new ListSeries(value.toString());
						data = new ArrayList<>();
					}
				}

				// Handle categories
				value = ClassUtils.getFieldValue(item, StringUtils.substringAfter(nameAttribute.getPath(), "."));
				if (value != null) {
					categories.add(value.toString());
				}

				// Handle data
				value = ClassUtils.getFieldValue(item, StringUtils.substringAfter(dataAttribute.getPath(), "."));
				if (value != null) {
					data.add((Number) value);
				}
			}
			if (!data.isEmpty()) {
				currentSeries.setData(data);
				series.add(currentSeries);
			}
			xaxis.setCategories(categories.toArray(new String[] {}));
		}
		return series;
	}

	protected Legend createLegend() {
		Legend legend = new Legend();
		legend.setLayout(LayoutDirection.VERTICAL);
		legend.setAlign(HorizontalAlign.RIGHT);
		legend.setVerticalAlign(VerticalAlign.TOP);
		// legend.setX(-100);
		// legend.setY(100);
		legend.setFloating(true);
		// legend.setBorderWidth(1);
		// legend.setBackgroundColor(new SolidColor("#FFFFFF"));
		legend.setShadow(true);
		return legend;
	}

	/**
	 * @return the seriesAttribute
	 */
	public AttributeModel getSeriesAttribute() {
		return seriesAttribute;
	}

	/**
	 * @param seriesAttribute
	 *            the seriesAttribute to set
	 */
	public void setSeriesAttribute(AttributeModel seriesAttribute) {
		this.seriesAttribute = seriesAttribute;
	}

	/**
	 * @return the nameAttribute
	 */
	public AttributeModel getNameAttribute() {
		return nameAttribute;
	}

	/**
	 * @param nameAttribute
	 *            the nameAttribute to set
	 */
	public void setNameAttribute(AttributeModel nameAttribute) {
		this.nameAttribute = nameAttribute;
	}

	/**
	 * @return the dataAttribute
	 */
	public AttributeModel getDataAttribute() {
		return dataAttribute;
	}

	/**
	 * @param dataAttribute
	 *            the dataAttribute to set
	 */
	public void setDataAttribute(AttributeModel dataAttribute) {
		this.dataAttribute = dataAttribute;
	}
}
