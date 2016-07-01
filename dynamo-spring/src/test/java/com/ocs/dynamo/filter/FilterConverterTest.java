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
package com.ocs.dynamo.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.test.MockUtil;

public class FilterConverterTest extends BaseMockitoTest {

    private EntityModelFactory emf = new EntityModelFactoryImpl();

    @Mock
    private MessageService messageService;

    private FilterConverter converter = new FilterConverter(null);

    private FilterConverter modelConverter;

    private com.vaadin.data.util.filter.Compare.Equal f1 = new com.vaadin.data.util.filter.Compare.Equal(
            "test", "test");

    private com.vaadin.data.util.filter.Compare.Equal f2 = new com.vaadin.data.util.filter.Compare.Equal(
            "test", "test");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockUtil.mockMessageService(messageService);
        modelConverter = new FilterConverter(emf.getModel(TestEntity.class));
    }

    @Test
    public void testCompareEqual() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Compare.Equal("test",
                "test"));
        Assert.assertTrue(result instanceof Compare.Equal);
    }

    @Test
    public void testCompareLess() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Compare.Less("test",
                "test"));
        Assert.assertTrue(result instanceof Compare.Less);
    }

    @Test
    public void testCompareLessOrEqual() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Compare.LessOrEqual(
                "test", "test"));
        Assert.assertTrue(result instanceof Compare.LessOrEqual);
    }

    @Test
    public void testCompareGreater() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Compare.Greater("test",
                "test"));
        Assert.assertTrue(result instanceof Compare.Greater);
    }

    @Test
    public void testCompareGreaterOrEqual() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Compare.GreaterOrEqual(
                "test", "test"));
        Assert.assertTrue(result instanceof Compare.GreaterOrEqual);
    }

    @Test
    public void testNot() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Not(f1));
        Assert.assertTrue(result instanceof Not);
    }

    @Test
    public void testAnd() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.And(f1, f2));
        Assert.assertTrue(result instanceof And);
    }

    @Test
    public void testOr() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Or(f1, f2));
        Assert.assertTrue(result instanceof Or);
    }

    @Test
    public void testBetween() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Between("test", 1, 10));
        Assert.assertTrue(result instanceof Between);
    }

    @Test
    public void testLike() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.Like("test", "%test%"));
        Assert.assertTrue(result instanceof Like);
    }

    /**
     * Simple string filter - case sensitive prefix only
     */
    @Test
    public void testSimpleStringFilter1() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.SimpleStringFilter(
                "test", "test", false, true));
        Assert.assertTrue(result instanceof Like);
        Like like = (Like) result;
        Assert.assertTrue(like.isCaseSensitive());
        Assert.assertEquals("test%", like.getValue());
    }

    /**
     * Simple string filter - case insensitive infix
     */
    @Test
    public void testSimpleStringFilter2() {
        Filter result = converter.convert(new com.vaadin.data.util.filter.SimpleStringFilter(
                "test", "test", true, false));
        Assert.assertTrue(result instanceof Like);
        Like like = (Like) result;
        Assert.assertFalse(like.isCaseSensitive());
        Assert.assertEquals("%test%", like.getValue());
    }

    /**
     * Test that a search for a details attribute is replaced by a Contains filter
     */
    @Test
    public void testSearchDetails() {
        Filter result = modelConverter.convert(new com.vaadin.data.util.filter.And(
                new com.vaadin.data.util.filter.Compare.Equal("testEntities", new TestEntity2())));

        Assert.assertTrue(result instanceof And);
        Filter first = ((And) result).getFilters().iterator().next();
        Assert.assertTrue(first instanceof Contains);
    }

    /**
     * Test that a search for a details attribute is replaced by a Contains filter
     */
    @Test
    public void testSearchDetails2() {
        List<TestEntity2> entities = Lists.newArrayList(new TestEntity2(), new TestEntity2());

        Filter result = modelConverter.convert(new com.vaadin.data.util.filter.And(
                new com.vaadin.data.util.filter.Compare.Equal("testEntities", entities)));

        Assert.assertTrue(result instanceof And);
        Filter first = ((And) result).getFilters().iterator().next();
        Assert.assertTrue(first instanceof Or);

        for (Filter f : ((Or) first).getFilters()) {
            Assert.assertTrue(f instanceof Contains);
        }
    }
}
