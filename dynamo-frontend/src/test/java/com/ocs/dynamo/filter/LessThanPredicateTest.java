package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class LessThanPredicateTest {

    @Test
    public void test() {
        LessThanPredicate<TestEntity> p1 = new LessThanPredicate<>("age", 20L);

        assertFalse(p1.test(null));

        TestEntity t1 = new TestEntity();
        t1.setAge(20L);
        assertFalse(p1.test(t1));

        t1.setAge(24L);
        assertFalse(p1.test(t1));

        t1.setAge(18L);
        assertTrue(p1.test(t1));
    }
}
