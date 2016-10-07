package com.ocs.jasperreports.chart;

import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.ui.RectangleEdge;

import com.ocs.dynamo.utils.ClassUtils;

/**
 * Axis that has not width left or right from a Category in the DomainAxis
 */
public class FullWidthCategoryDomainAxis extends CategoryAxis {

	private static final long serialVersionUID = 1L;

	public FullWidthCategoryDomainAxis(CategoryAxis domainAxis) {
		ClassUtils.copyFields(domainAxis, this);
	}

	@Override
	protected double calculateCategorySize(int categoryCount, Rectangle2D area, RectangleEdge edge) {
		return super.calculateCategorySize(categoryCount, area, edge) + super.getCategoryMargin();
	}

	@Override
	public double getCategoryMargin() {
		return 0;
	}

	@Override
	protected double calculateCategoryGapSize(int categoryCount, Rectangle2D area,
			RectangleEdge edge) {
		return 0;
	}
}
