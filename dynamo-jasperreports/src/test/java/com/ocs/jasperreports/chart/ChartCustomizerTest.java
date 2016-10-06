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

import net.sf.jasperreports.engine.fill.JRBaseFiller;
import net.sf.jasperreports.engine.fill.JRFillChart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYZDataset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Patrick Deenen (patrick@opencircle.solutions)
 *
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class ChartCustomizerTest {

    DefaultXYZDataset dataset = new DefaultXYZDataset();
    JFreeChart chart = ChartFactory.createBubbleChart("Title", "X", "Y", dataset);
    @Mock
    JRFillChart jrChart;
    @Mock
    JRBaseFiller filler;
    ChartCustomizer customizer = new ChartCustomizer();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
	Mockito.when(jrChart.getKey()).thenReturn("BubbleChart");
	customizer.init(filler, jrChart);

	double[][] data = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
	dataset.addSeries("Series", data);
    }

    /**
     * Test method for
     * {@link com.ocs.jasperreports.chart.ChartCustomizer#customize(org.jfree.chart.JFreeChart, net.sf.jasperreports.engine.JRChart)}
     * .
     */
    @Test
    public void testLabels() {
	Mockito.when(filler.getVariableValue("BubbleChart.Labels")).thenReturn(Boolean.TRUE);
	customizer.customize(chart, jrChart);
	Assert.fail("TODO");
    }

    /**
     * Test method for
     * {@link com.ocs.jasperreports.chart.ChartCustomizer#customize(org.jfree.chart.JFreeChart, net.sf.jasperreports.engine.JRChart)}
     * .
     */
    @Test
    public void testMarkers() {
	// Mockito.when(filler.getVariableValue("BubbleChart.MarkerRange")).thenReturn();
	// Mockito.when(filler.getVariableValue("BubbleChart.MarkerDomain")).thenReturn();
	Assert.fail("TODO");
    }

    /**
     * Test method for
     * {@link com.ocs.jasperreports.chart.ChartCustomizer#customize(org.jfree.chart.JFreeChart, net.sf.jasperreports.engine.JRChart)}
     * .
     */
    @Test
    public void testQuadrant() {
	// Mockito.when(filler.getVariableValue("BubbleChart.Quadrant")).thenReturn();
	Assert.fail("TODO");
    }
}
