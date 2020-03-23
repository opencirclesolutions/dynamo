package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class NotPredicateTest {

    @Test
    public void test() {

        EqualsPredicate<TestEntity> p1 = new EqualsPredicate<>("name", "Bob");
        NotPredicate<TestEntity> not = new NotPredicate<>(p1);

        TestEntity t1 = new TestEntity();
        t1.setName("Bob");
        assertFalse(not.test(t1));

        t1.setName("Rob");
        assertTrue(not.test(t1));
    }
}
