//package com.ocs.dynamo.ui.component;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import com.ocs.dynamo.domain.TestEntity;
//import com.vaadin.v7.data.util.BeanItem;
//
//public class IgnoreDiacriticsStringFilterTest {
//
//    @Test
//    public void testPassesFilter() {
//
//        TestEntity entity = new TestEntity();
//        entity.setName("Kevin");
//
//        BeanItem<TestEntity> bItem = new BeanItem<>(entity);
//
//        IgnoreDiacriticsStringFilter filter = new IgnoreDiacriticsStringFilter("name", "e", true,
//                false);
//        Assert.assertTrue(filter.passesFilter(null, bItem));
//
//        filter = new IgnoreDiacriticsStringFilter("name", "x", true, false);
//        Assert.assertFalse(filter.passesFilter(null, bItem));
//
//        // case sensitive
//        filter = new IgnoreDiacriticsStringFilter("name", "E", false, false);
//        Assert.assertFalse(filter.passesFilter(null, bItem));
//
//        // prefix only
//        filter = new IgnoreDiacriticsStringFilter("name", "e", true, true);
//        Assert.assertFalse(filter.passesFilter(null, bItem));
//    }
//
//    @Test
//    public void testPassesFilterIgnoreDiacritics() {
//
//        TestEntity entity = new TestEntity();
//        entity.setName("Këvin");
//
//        BeanItem<TestEntity> bItem = new BeanItem<>(entity);
//
//        IgnoreDiacriticsStringFilter filter = new IgnoreDiacriticsStringFilter("name", "ev", true,
//                false);
//        Assert.assertTrue(filter.passesFilter(null, bItem));
//    }
//
//    @Test
//    public void testEquals() {
//        IgnoreDiacriticsStringFilter filter = new IgnoreDiacriticsStringFilter("name", "ev", true,
//                false);
//        IgnoreDiacriticsStringFilter filter2 = new IgnoreDiacriticsStringFilter("name", "ev", true,
//                false);
//        IgnoreDiacriticsStringFilter filter3 = new IgnoreDiacriticsStringFilter("age", "ev", true,
//                false);
//        IgnoreDiacriticsStringFilter filter4 = new IgnoreDiacriticsStringFilter("name", "bev", true,
//                false);
//        IgnoreDiacriticsStringFilter filter5 = new IgnoreDiacriticsStringFilter("name", "ev",
//                false, false);
//        IgnoreDiacriticsStringFilter filter6 = new IgnoreDiacriticsStringFilter("name", "ev", true,
//                true);
//
//        Assert.assertTrue(filter.equals(filter));
//        Assert.assertFalse(filter.equals(new Object()));
//        Assert.assertTrue(filter.equals(filter2));
//        Assert.assertFalse(filter.equals(filter3));
//        Assert.assertFalse(filter.equals(filter4));
//        Assert.assertFalse(filter.equals(filter5));
//        Assert.assertFalse(filter.equals(filter6));
//    }
//}
