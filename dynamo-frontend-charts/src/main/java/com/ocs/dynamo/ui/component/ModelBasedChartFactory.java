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
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.ChartEntityModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.ui.container.ServiceQueryDefinition;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.addon.charts.Chart;
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
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;

/**
 * Vaadin chart which is built using the entitymodel.
 * 
 * @author Patrick.Deenen@OpenCircle.Solutions
 *
 */
public class ModelBasedChartFactory implements FieldFactory {

	public enum ChartType {
		BAR(com.vaadin.addon.charts.model.ChartType.BAR), COLUMN(com.vaadin.addon.charts.model.ChartType.COLUMN), LINE(
				com.vaadin.addon.charts.model.ChartType.LINE);

		private com.vaadin.addon.charts.model.ChartType chartType;

		/**
		 * @return the chartType
		 */
		public com.vaadin.addon.charts.model.ChartType getChartType() {
			return chartType;
		}

		ChartType(com.vaadin.addon.charts.model.ChartType chartType) {
			this.chartType = chartType;
		}
	};

	private Chart chart;
	private AttributeModel seriesAttribute;
	private AttributeModel nameAttribute;
	private AttributeModel dataAttribute;
	private String title;
	private String subTitle;
	// TODO Get data tooltip from ChartAttributeModel
	private String tooltip = "this.series.name +': '+ this.x + ',' +this.y";
	private Container container;
	private XAxis xaxis;
	private YAxis yaxis;
	private ArrayList<Series> series;

	public ModelBasedChartFactory() {
		super();
	}

	/**
	 * Use this constructor when you have a ServiceContainer based on a GraphEntityModel
	 *
	 * @param container
	 *            The service container with GraphEntityModel
	 */
	public ModelBasedChartFactory(ServiceContainer<?, ?> container) {
		this(container,
				(ChartEntityModel<?>) (((ServiceQueryDefinition<?, ?>) container.getQueryView().getQueryDefinition())
						.getEntityModel()));
	}

	/**
	 * Use this constructor when you have a Container and ChartEntityModel
	 *
	 * @param container
	 * @param chartEntityModel
	 */
	public ModelBasedChartFactory(Container container, ChartEntityModel<?> chartEntityModel) {
		super();
		this.container = container;
		init(chartEntityModel);
	}

	/**
	 * Use this constructor when you have a ServiceContainer based on a normal EntityModel
	 * 
	 * @param container
	 *            The service container with EntityModel
	 */
	public ModelBasedChartFactory(ServiceContainer<?, ?> container, String seriesAttribute, String nameAttribute,
			String dataAttribute) {
		super();
		this.container = container;
		EntityModel<?> entityModel = ((ServiceQueryDefinition<?, ?>) container.getQueryView().getQueryDefinition())
				.getEntityModel();
		this.title = entityModel.getDisplayName();
		this.seriesAttribute = entityModel.getAttributeModel(seriesAttribute);
		this.nameAttribute = entityModel.getAttributeModel(nameAttribute);
		this.dataAttribute = entityModel.getAttributeModel(dataAttribute);
	}

	/**
	 * Use this constructor when you have a normal Container and given attribute models
	 * 
	 * @param container
	 */
	public ModelBasedChartFactory(Container container, AttributeModel seriesAttribute, AttributeModel nameAttribute,
			AttributeModel dataAttribute) {
		super();
		this.seriesAttribute = seriesAttribute;
		this.nameAttribute = nameAttribute;
		this.dataAttribute = dataAttribute;
		this.container = container;
	}

	public Chart create() {

		VaadinUtils.addPropertyIdToContainer(container, seriesAttribute);
		VaadinUtils.addPropertyIdToContainer(container, nameAttribute);
		VaadinUtils.addPropertyIdToContainer(container, dataAttribute);

		createSeriesByDataCopy();

		if (StringUtils.isBlank(title)) {
			title = dataAttribute.getEntityModel().getDisplayName();
		}
		chart = createChart(ChartType.BAR);
		return chart;
	}

	/**
	 * This method assumes the container is sorted by "series, name, y" and that all categories are available in the
	 * first series found. Also creates the x and y axis.
	 * 
	 * @return The created series
	 */
	protected ArrayList<Series> createSeriesByDataCopy() {
		series = new ArrayList<>();
		if (nameAttribute != null && dataAttribute != null
				&& Number.class.isAssignableFrom(dataAttribute.getNormalizedType())) {
			xaxis = new XAxis();
			xaxis.setTitle(nameAttribute.getDisplayName());
			yaxis = new YAxis();
			yaxis.setTitle(nameAttribute.getDisplayName());
			HashSet<String> categories = new HashSet<>();
			ListSeries currentSeries = new ListSeries();
			ArrayList<Number> data = new ArrayList<>();

			for (Object itemId : container.getItemIds()) {

				// Handle series creation
				Property<?> p = container.getContainerProperty(itemId, seriesAttribute.getPath());
				if (p != null && p.getValue() != null) {
					if (currentSeries.getName() == null) {
						// Init first created series
						currentSeries.setName(p.getValue().toString());
					} else if (!currentSeries.getName().equals(p.getValue().toString())) {
						// Found new name, hence finish current series and starting creating new
						if (!data.isEmpty()) {
							currentSeries.setData(data);
							series.add(currentSeries);
						}
						currentSeries = new ListSeries(p.getValue().toString());
						data = new ArrayList<>();
					}
				}

				// Handle categories
				p = container.getContainerProperty(itemId, nameAttribute.getPath());
				if (p != null && p.getValue() != null) {
					categories.add(p.getValue().toString());
				}

				// Handle data
				p = container.getContainerProperty(itemId, dataAttribute.getPath());
				if (p != null && p.getValue() != null) {
					data.add((Number) p.getValue());
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

	/*
	protected XAxis createXAxis() {
		xaxis = new XAxis();
		if (container != null && nameAttribute != null && nameAttribute.isVisibleInTable()
				&& container.getContainerPropertyIds().contains(nameAttribute.getPath())) {
			xaxis.setTitle(nameAttribute.getDisplayName());
			HashSet<String> categories = new HashSet<>();
			for (Object itemId : container.getItemIds()) {
				@SuppressWarnings("unchecked")
				Property<Object> p = container.getItem(itemId).getItemProperty(nameAttribute.getPath());
				if (p != null && p.getValue() != null) {
					categories.add(p.getValue().toString());
				}
			}
			xaxis.setCategories(categories.toArray(new String[] {}));
		}
		return xaxis;
	}
*/
	
	protected Chart createChart(ChartType chartType) {
		chart = new Chart(chartType.getChartType());

		Configuration conf = chart.getConfiguration();

		if (!StringUtils.isBlank(title)) {
			conf.setTitle(title);
		}
		if (!StringUtils.isBlank(subTitle)) {
			conf.setSubTitle(subTitle);
		}

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

		conf.setSeries(series);

		chart.drawChart(conf);
		return chart;
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

	@Override
	public Field<?> constructField(Context context) {
		ChartField<Object> chartField = null;
		if (context.getAttributeModel() != null && context.getAttributeModel().getNestedEntityModel() != null
				&& context.getAttributeModel().getNestedEntityModel() instanceof ChartEntityModel) {
			// Create from model
			ChartEntityModel<?> gem = (ChartEntityModel<?>) context.getAttributeModel().getNestedEntityModel();
			chartField = new ChartField<Object>();
			chartField.setSeriesAttribute(gem.getSeriesAttributeModel());
			chartField.setNameAttribute(gem.getNameAttributeModel());
			chartField.setDataAttribute(gem.getDataAttributeModel());
			chartField.createChart(null, gem.getDataAttributeModel().getEntityModel().getDisplayName(),
					gem.getSubTitle(), gem.getTooltip());
		}
		return chartField;
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
	public ModelBasedChartFactory setSeriesAttribute(AttributeModel seriesAttribute) {
		this.seriesAttribute = seriesAttribute;
		return this;
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
	public ModelBasedChartFactory setNameAttribute(AttributeModel nameAttribute) {
		this.nameAttribute = nameAttribute;
		return this;
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
	public ModelBasedChartFactory setDataAttribute(AttributeModel dataAttribute) {
		this.dataAttribute = dataAttribute;
		return this;
	}

	/**
	 * @return the container
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * @param container
	 *            the container to set
	 */
	public ModelBasedChartFactory setContainer(Container container) {
		init(container);
		return this;
	}

	protected void init(Container container) {
		this.container = container;
		if (container instanceof ServiceContainer<?, ?>) {
			ChartEntityModel<?> chartEntityModel = (ChartEntityModel<?>) (((ServiceQueryDefinition<?, ?>) ((ServiceContainer<?, ?>) container)
					.getQueryView().getQueryDefinition()).getEntityModel());
			init(chartEntityModel);
		}
	}

	protected void init(ChartEntityModel<?> chartEntityModel) {
		if (chartEntityModel != null) {
			this.title = chartEntityModel.getDisplayName();
			this.subTitle = chartEntityModel.getSubTitle();
			this.tooltip = chartEntityModel.getTooltip();
			this.seriesAttribute = chartEntityModel.getSeriesAttributeModel();
			this.nameAttribute = chartEntityModel.getNameAttributeModel();
			this.dataAttribute = chartEntityModel.getDataAttributeModel();
		} else {
			this.title = null;
			this.subTitle = null;
			this.tooltip = null;
			this.seriesAttribute = null;
			this.nameAttribute = null;
			this.dataAttribute = null;
		}
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public ModelBasedChartFactory setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * @return the subTitle
	 */
	public String getSubTitle() {
		return subTitle;
	}

	/**
	 * @param subTitle
	 *            the subTitle to set
	 */
	public ModelBasedChartFactory setSubTitle(String subTitle) {
		this.subTitle = subTitle;
		return this;
	}

	/**
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @param tooltip
	 *            the tooltip to set
	 */
	public ModelBasedChartFactory setTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	/**
	 * @return the chart
	 */
	protected Chart getChart() {
		return chart;
	}

	/**
	 * @param chart
	 *            the chart to set
	 */
	protected ModelBasedChartFactory setChart(Chart chart) {
		this.chart = chart;
		return this;
	}

	/**
	 * @return the series
	 */
	protected ArrayList<Series> getSeries() {
		return series;
	}

	/**
	 * @param series
	 *            the series to set
	 */
	protected ModelBasedChartFactory setSeries(ArrayList<Series> series) {
		this.series = series;
		return this;
	}

	/**
	 * @return the xaxis
	 */
	protected XAxis getXaxis() {
		return xaxis;
	}

	/**
	 * @param xaxis
	 *            the xaxis to set
	 */
	protected ModelBasedChartFactory setXaxis(XAxis xaxis) {
		this.xaxis = xaxis;
		return this;
	}

	/**
	 * @return the yaxis
	 */
	protected YAxis getYaxis() {
		return yaxis;
	}

	/**
	 * @param yaxis
	 *            the yaxis to set
	 */
	protected ModelBasedChartFactory setYaxis(YAxis yaxis) {
		this.yaxis = yaxis;
		return this;
	}

}
