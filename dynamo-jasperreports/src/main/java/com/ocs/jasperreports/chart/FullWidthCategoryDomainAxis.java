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
	protected double calculateCategoryGapSize(int categoryCount, Rectangle2D area, RectangleEdge edge) {
		return 0;
	}
}
