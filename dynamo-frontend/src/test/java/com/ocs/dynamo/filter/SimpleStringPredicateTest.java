package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class SimpleStringPredicateTest {

    @Test
    public void testPrefixCaseSensitive() {
        SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", true, true);

        assertFalse(p1.test(null));

        TestEntity t1 = new TestEntity();
        t1.setName("Kevin");
        assertFalse(p1.test(t1));

        // case match at start
        t1.setName("evin");
        assertTrue(p1.test(t1));

        // case does not match
        t1.setName("Evin");
        assertFalse(p1.test(t1));
    }

    @Test
    public void testPrefixCaseInsensitive() {
        SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", true, false);

        assertFalse(p1.test(null));

        TestEntity t1 = new TestEntity();
        t1.setName("Kevin");
        assertFalse(p1.test(t1));

        // case match at start
        t1.setName("evin");
        assertTrue(p1.test(t1));

        // case does not match
        t1.setName("Evin");
        assertTrue(p1.test(t1));
    }

    @Test
    public void testAnyWhereCaseSensitive() {
        SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", false, true);

        assertFalse(p1.test(null));

        TestEntity t1 = new TestEntity();
        t1.setName("Kevin");
        assertTrue(p1.test(t1));

        // case match at start
        t1.setName("evin");
        assertTrue(p1.test(t1));

        // case does not match
        t1.setName("Evin");
        assertFalse(p1.test(t1));
    }

    @Test
    public void testAnyWhereCaseInsensitive() {
        SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", false, false);

        assertFalse(p1.test(null));

        TestEntity t1 = new TestEntity();
        t1.setName("Kevin");
        assertTrue(p1.test(t1));

        // case match at start
        t1.setName("evin");
        assertTrue(p1.test(t1));

        // case does not match
        t1.setName("Evin");
        assertTrue(p1.test(t1));
    }
}
