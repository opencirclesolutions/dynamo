package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class AndPredicateTest {

    @Test
    public void test() {

        EqualsPredicate<TestEntity> p1 = new EqualsPredicate<>("name", "Bob");
        EqualsPredicate<TestEntity> p2 = new EqualsPredicate<>("age", 44L);

        AndPredicate<TestEntity> and = new AndPredicate<>(p1);

        TestEntity t1 = new TestEntity();
        t1.setName("Bob");
        t1.setAge(45L);
        assertTrue(and.test(t1));

        and = new AndPredicate<>(p1, p2);
        assertFalse(and.test(t1));

        t1.setAge(44L);
        assertTrue(and.test(t1));
    }
}
