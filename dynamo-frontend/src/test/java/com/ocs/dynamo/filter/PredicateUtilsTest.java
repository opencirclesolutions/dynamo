package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;
import com.vaadin.flow.function.SerializablePredicate;

public class PredicateUtilsTest {

    @Test
    public void testIsTrue() {

        EqualsPredicate<TestEntity> eq = new EqualsPredicate<>("prop1", Boolean.TRUE);
        assertTrue(PredicateUtils.isTrue(eq, "prop1"));

        eq = new EqualsPredicate<TestEntity>("prop1", Boolean.FALSE);
        assertFalse(PredicateUtils.isTrue(eq, "prop1"));

        EqualsPredicate<TestEntity> eq2 = new EqualsPredicate<>("prop1", "someString");
        assertFalse(PredicateUtils.isTrue(eq2, "prop1"));

        // only works for Equal
        GreaterThanPredicate<TestEntity> gt = new GreaterThanPredicate<>("prop1", Boolean.TRUE);
        assertFalse(PredicateUtils.isTrue(gt, "prop1"));
    }

    @Test
    public void testExtractPredicate_Compare() {

        EqualsPredicate<TestEntity> comp = new EqualsPredicate<>("prop1", "someString");
        SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(comp, "prop1");
        assertNotNull(f1);

        // wrong property
        SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(comp, "prop2");
        assertNull(f2);
    }

    @Test
    public void testExtractPredicate_CompareWrongType() {

        EqualsPredicate<TestEntity> comp = new EqualsPredicate<>("prop1", "someString");
        SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(comp, "prop1", EqualsPredicate.class);
        assertNotNull(f1);

        // wrong property
        comp = new EqualsPredicate<>("prop1", "someString");
        f1 = PredicateUtils.extractPredicate(comp, "prop1", GreaterOrEqualPredicate.class);
        assertNull(f1);
    }

    @Test
    public void testExtractPredicate_Not() {

        EqualsPredicate<TestEntity> comp = new EqualsPredicate<>("prop1", "someString");
        NotPredicate<TestEntity> not = new NotPredicate<>(comp);

        SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(not, "prop1");
        assertNotNull(f1);

        // wrong property
        SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(not, "prop2");
        assertNull(f2);
    }

    @Test
    public void testExtractPredicate_Like() {

        LikePredicate<TestEntity> like = new LikePredicate<TestEntity>("prop1", "someString", true);
        SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(like, "prop1");
        assertNotNull(f1);

        // wrong property
        SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(like, "prop2");
        assertNull(f2);
    }

    @Test
    public void testExtractPredicate_SimpleString() {

        SimpleStringPredicate<TestEntity> ssf = new SimpleStringPredicate<>("prop1", "someString", true, true);
        SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(ssf, "prop1");
        assertNotNull(f1);

        // wrong property
        SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(ssf, "prop2");
        assertNull(f2);
    }

    @Test
    public void testExtractPredicate_Composite() {

        LikePredicate<TestEntity> compare = new LikePredicate<>("prop1", "someString", true);
        AndPredicate<TestEntity> and = new AndPredicate<TestEntity>(compare, new EqualsPredicate<TestEntity>("prop2", "someString"));

        // first operand
        SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(and, "prop1");
        assertNotNull(f1);

        // second operand
        SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(and, "prop2");
        assertNotNull(f2);

        // wrong property
        SerializablePredicate<TestEntity> f3 = PredicateUtils.extractPredicate(compare, "prop3");
        assertNull(f3);
    }

    @Test
    public void testIsPredicateValueSet() {
        assertEquals(false, PredicateUtils.isPredicateValueSet(null, new HashSet<>()));

        GreaterOrEqualPredicate<TestEntity> empty = new GreaterOrEqualPredicate<TestEntity>("test", null);
        assertFalse(PredicateUtils.isPredicateValueSet(empty, new HashSet<>()));

        GreaterOrEqualPredicate<TestEntity> eq = new GreaterOrEqualPredicate<TestEntity>("test", "bob");
        assertTrue(PredicateUtils.isPredicateValueSet(eq, new HashSet<>()));
        assertFalse(PredicateUtils.isPredicateValueSet(eq, Set.of("test")));

        // test with an "and" filter
        assertTrue(PredicateUtils.isPredicateValueSet(new AndPredicate<TestEntity>(eq, empty), new HashSet<>()));

        // test with a String filter
        assertFalse(PredicateUtils.isPredicateValueSet(new SimpleStringPredicate<>("test", null, true, true), new HashSet<>()));
        assertTrue(PredicateUtils.isPredicateValueSet(new SimpleStringPredicate<TestEntity>("test", "abc", true, true), new HashSet<>()));

        // test with a "Like" filter
        assertFalse(PredicateUtils.isPredicateValueSet(new LikePredicate<>("test", null, true), new HashSet<>()));
        assertTrue(PredicateUtils.isPredicateValueSet(new LikePredicate<TestEntity>("test", "abc", true), new HashSet<>()));

        // test with a "Between" filter
        assertFalse(PredicateUtils.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", null, null), new HashSet<>()));
        assertTrue(PredicateUtils.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", "abc", null), new HashSet<>()));
        assertTrue(PredicateUtils.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", null, "def"), new HashSet<>()));
    }

    @Test
    public void testExtractPredicateValue_Compare() {

        EqualsPredicate<TestEntity> comp = new EqualsPredicate<TestEntity>("prop1", "someString");
        Object value = PredicateUtils.extractPredicateValue(comp, "prop1");
        assertEquals("someString", value);

        // wrong property
        value = PredicateUtils.extractPredicateValue(comp, "prop2");
        assertNull(value);
    }

    @Test
    public void testExtractPredicateValue_Composite() {

        LikePredicate<TestEntity> compare = new LikePredicate<TestEntity>("prop1", "someString1", true);
        AndPredicate<TestEntity> and = new AndPredicate<TestEntity>(compare, new EqualsPredicate<TestEntity>("prop2", "someString2"));

        // first operand
        Object f1 = PredicateUtils.extractPredicateValue(and, "prop1");
        assertEquals("someString1", f1);

        // second operand
        Object f2 = PredicateUtils.extractPredicateValue(and, "prop2");
        assertEquals("someString2", f2);

        // wrong property
        Object f3 = PredicateUtils.extractPredicateValue(compare, "prop3");
        assertNull(f3);
    }
}
