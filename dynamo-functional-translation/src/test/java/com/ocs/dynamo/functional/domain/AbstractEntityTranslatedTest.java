package com.ocs.dynamo.functional.domain;

import org.junit.Assert;
import org.junit.Test;

public class AbstractEntityTranslatedTest {

    @Test
    public void test() {
        Product product = new Product();
        Locale locale1 = new Locale("nl", "Nederlands");
        Locale locale2 = new Locale("en", "Engels");

        String name = Product.TranslatedFields.NAME.toString();

        ProductTranslation pt = new ProductTranslation();
        pt.setField(name);
        pt.setLocale(locale1);
        pt.setTranslation("Pingpongbal");
        product.addTranslation(pt);

        ProductTranslation pt2 = new ProductTranslation();
        pt2.setField(name);
        pt2.setLocale(locale2);
        pt2.setTranslation("Pingpong ball");
        product.addTranslation(pt2);

        Assert.assertEquals(2, product.getTranslations().size());
        Assert.assertEquals(2, product.getTranslations(name).size());

        Assert.assertEquals("Pingpongbal", product.getTranslation(name, locale1).getTranslation());
        Assert.assertEquals("Pingpong ball", product.getTranslation(name, locale2).getTranslation());

        Assert.assertEquals("Pingpongbal", product.getTranslation(name, "nl").getTranslation());
        Assert.assertEquals("Pingpong ball", product.getTranslation(name, "en").getTranslation());
    }

    @Test
    public void testIsTranslationsDoesNotContainDuplicates() {
        String name = Product.TranslatedFields.NAME.toString();

        Product product = new Product();
        Locale locale1 = new Locale("nl", "Nederlands");

        ProductTranslation pt = new ProductTranslation();
        pt.setField(name);
        pt.setLocale(locale1);
        pt.setTranslation("Pingpongbal");
        product.addTranslation(pt);
        Assert.assertTrue(product.isTranslationsDoesNotContainDuplicates());

        ProductTranslation pt2 = new ProductTranslation();
        pt2.setField(name);
        pt2.setLocale(locale1);
        pt2.setTranslation("Pingpongbal");
        product.addTranslation(pt2);

        Assert.assertFalse(product.isTranslationsDoesNotContainDuplicates());
    }

    @Test
    public void testIsValidRequiredTranslations() {
        String name = Product.TranslatedFields.NAME.toString();

        Product product = new Product();
        Locale locale1 = new Locale("nl", "Nederlands");
        Locale locale2 = new Locale("en", "Engels");

        Assert.assertFalse(product.isValidRequiredTranslations());

        ProductTranslation pt = new ProductTranslation();
        pt.setField(name);
        pt.setLocale(locale1);
        pt.setTranslation("Pingpongbal");
        product.addTranslation(pt);

        ProductTranslation pt2 = new ProductTranslation();
        pt2.setField(name);
        pt2.setLocale(locale2);
        pt2.setTranslation("Pingpong ball");
        product.addTranslation(pt2);

        Assert.assertTrue(product.isValidRequiredTranslations());
    }
}
