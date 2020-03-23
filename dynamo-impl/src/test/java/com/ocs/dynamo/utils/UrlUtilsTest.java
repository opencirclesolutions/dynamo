package com.ocs.dynamo.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

public class UrlUtilsTest {

    @Test
    public void testCreateUrl() throws URISyntaxException {
        URI uri = UrlUtils.createUrl("http://somehost/path?k0=ã ã", "k1", "ão 355(KM16,5)");
        assertNotNull(uri);
        assertTrue(uri.toString().indexOf("%20") == 25);

        uri = UrlUtils.createUrl("http://somehost/path", "k1", "ão 355(KM16,5)");
        assertNotNull(uri);
        assertTrue(uri.toString().indexOf("%20") == 26);

        uri = UrlUtils.createUrl("//somehost/path", "k1", "ão 355(KM16,5)");
        assertNotNull(uri);
        assertTrue(uri.toString().indexOf("%20") == 26);

        uri = UrlUtils.createUrl("//somehost/path", "k1", "ão 355(KM16,5)", "k2", " & % $ ? ");
        assertNotNull(uri);
        assertTrue(uri.toString().indexOf("%20") == 26);
    }

}
