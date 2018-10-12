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

import com.ocs.dynamo.domain.Population;
import com.ocs.dynamo.domain.WorldPopulation;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.ChartEntityModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.impl.DefaultServiceImpl;
import com.ocs.dynamo.test.BaseIntegrationTest;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.vaadin.addon.charts.Chart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

public class ModelBasedChartFactoryIntegrationTest extends BaseIntegrationTest {

    @Inject
	private DefaultServiceImpl<Integer, Population> populationService;

    @Inject
    private EntityModelFactory entityModelFactory;

    @Before
    public void setup() {
		Population entity = new Population();
		entity.setId(1);
		entity.setPopulation(1000);
		entity.setRegion("Europe");
		entity.setYear(2010);
		entity.setVersion(1);
		entity = populationService.save(entity);

		entity = new Population();
		entity.setId(2);
		entity.setPopulation(1100);
		entity.setRegion("Europe");
		entity.setYear(2011);
		entity.setVersion(1);
		entity = populationService.save(entity);

		entity = new Population();
		entity.setId(3);
		entity.setPopulation(1400);
		entity.setRegion("Europe");
		entity.setYear(2013);
		entity.setVersion(1);
		entity = populationService.save(entity);

		entity = new Population();
		entity.setId(4);
		entity.setPopulation(2000);
		entity.setRegion("Africa");
		entity.setYear(2011);
		entity.setVersion(1);
		entity = populationService.save(entity);
    }

    /**
     * Test the working of a model based table combined with a service container using an ID-based
     * query
     */
    @Test
	public void testChartFactory() {
		ChartEntityModel<Population> model = (ChartEntityModel<Population>) entityModelFactory
				.getModel(Population.class);
		Assert.assertNotNull(model);
		Assert.assertNotNull(model.getDisplayName());
		Assert.assertNotNull(model.getSubTitle());
		Assert.assertNotNull(model.getSeriesAttributeModel());
		Assert.assertNotNull(model.getNameAttributeModel());
		Assert.assertNotNull(model.getDataAttributeModel());
		Assert.assertNotNull(model.getTooltip());

		ServiceContainer<Integer, Population> container = new ServiceContainer<>(populationService,
				model, 20, QueryType.ID_BASED);
        Assert.assertNotNull(container.getService());

		Chart chart = new ModelBasedChartFactory(container).create();
		Assert.assertNotNull(chart);
		Assert.assertTrue(container.getContainerPropertyIds().contains(model.getSeriesAttributeModel().getPath()));
		Assert.assertTrue(container.getContainerPropertyIds().contains(model.getNameAttributeModel().getPath()));
		Assert.assertTrue(container.getContainerPropertyIds().contains(model.getDataAttributeModel().getPath()));
		Assert.assertNotNull(chart.getConfiguration());
		Assert.assertNotNull(chart.getConfiguration().getxAxis());
		Assert.assertNotNull(chart.getConfiguration().getyAxis());
		Assert.assertNotNull(chart.getConfiguration().getTitle());
		Assert.assertNotNull(chart.getConfiguration().getSubTitle());
		Assert.assertNotNull(chart.getConfiguration().getTooltip());
		Assert.assertNotNull(chart.getConfiguration().getSeries());
		Assert.assertFalse(chart.getConfiguration().getSeries().isEmpty());
    }

	@Test
	public void testChartFactoryNested() {
		EntityModel<WorldPopulation> wpmodel = entityModelFactory.getModel(WorldPopulation.class);
		Assert.assertNotNull(wpmodel);

		AttributeModel am = wpmodel.getAttributeModel("populations");
		Assert.assertNotNull(am);

		EntityModel<?> model = am.getNestedEntityModel();
		Assert.assertNotNull(model);
		Assert.assertNotNull(model.getDisplayName());
		Assert.assertTrue(model instanceof ChartEntityModel);
		ChartEntityModel<?> gem = (ChartEntityModel<?>) model;
		Assert.assertNotNull(gem.getSubTitle());
		Assert.assertNotNull(gem.getSeriesAttributeModel());
		Assert.assertNotNull(gem.getNameAttributeModel());
		Assert.assertNotNull(gem.getDataAttributeModel());
		Assert.assertNotNull(gem.getTooltip());
	}
}
