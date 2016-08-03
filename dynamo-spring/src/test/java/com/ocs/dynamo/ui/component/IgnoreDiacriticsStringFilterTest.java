package com.ocs.dynamo.ui.component;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.vaadin.data.util.BeanItem;

public class IgnoreDiacriticsStringFilterTest {

    @Test
    public void testPassesFilter() {

        TestEntity entity = new TestEntity();
        entity.setName("Kevin");

        BeanItem<TestEntity> bItem = new BeanItem<>(entity);

        IgnoreDiacriticsStringFilter filter = new IgnoreDiacriticsStringFilter("name", "e", true,
                false);
        Assert.assertTrue(filter.passesFilter(null, bItem));

        filter = new IgnoreDiacriticsStringFilter("name", "x", true, false);
        Assert.assertFalse(filter.passesFilter(null, bItem));

        // case sensitive
        filter = new IgnoreDiacriticsStringFilter("name", "E", false, false);
        Assert.assertFalse(filter.passesFilter(null, bItem));

        // prefix only
        filter = new IgnoreDiacriticsStringFilter("name", "e", true, true);
        Assert.assertFalse(filter.passesFilter(null, bItem));
    }

    @Test
    public void testPassesFilterIgnoreDiacritics() {

        TestEntity entity = new TestEntity();
        entity.setName("Këvin");

        BeanItem<TestEntity> bItem = new BeanItem<>(entity);

        IgnoreDiacriticsStringFilter filter = new IgnoreDiacriticsStringFilter("name", "ev", true,
                false);
        Assert.assertTrue(filter.passesFilter(null, bItem));

    }
}
