package com.ocs.dynamo.functional.domain;

import org.junit.Assert;
import org.junit.Test;

public class DomainTest {

    @Test
    public void testEquals() {

        Country c1 = new Country("NL", "Nederland");
        c1.setId(1);

        Country c2 = new Country("NL", "Nederland");
        c2.setId(1);

        Country c3 = new Country("NL", "Nederland");
        Country c4 = new Country("NL", "Nederland");

        Region r1 = new Region(null, "Europe");
        r1.setId(1);

        Assert.assertFalse(c1.equals(null));
        Assert.assertFalse(c1.equals(new Object()));

        // IDs match but class is different
        Assert.assertFalse(c1.equals(r1));

        // IDs are the same
        Assert.assertTrue(c1.equals(c2));
        // no IDs, check the code and type instead
        Assert.assertTrue(c3.equals(c4));

        // partial IDs
        Assert.assertTrue(c1.equals(c4));
    }
}
