package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class InPredicateTest {

    @Test
    public void test() {
        InPredicate<TestEntity> p1 = new InPredicate<TestEntity>("age", List.of(4L, 5L, 6L));

        assertFalse(p1.test(null));

        TestEntity t1 = new TestEntity();
        t1.setAge(7L);

        assertFalse(p1.test(t1));

        t1.setAge(4L);
        assertTrue(p1.test(t1));
    }
}
