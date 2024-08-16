package org.dynamoframework.utils;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UrlUtilsTest {

    @Test
    public void testCreateUrl() throws URISyntaxException {
        URI uri = UrlUtils.createUrl("http://somehost/path?k0=ã ã", "k1", "ão 355(KM16,5)");
        assertNotNull(uri);
        assertEquals(25, uri.toString().indexOf("%20"));

        uri = UrlUtils.createUrl("http://somehost/path", "k1", "ão 355(KM16,5)");
        assertNotNull(uri);
        assertEquals(26, uri.toString().indexOf("%20"));

        uri = UrlUtils.createUrl("//somehost/path", "k1", "ão 355(KM16,5)");
        assertNotNull(uri);
        assertEquals(26, uri.toString().indexOf("%20"));

        uri = UrlUtils.createUrl("//somehost/path", "k1", "ão 355(KM16,5)", "k2", " & % $ ? ");
        assertNotNull(uri);
        assertEquals(26, uri.toString().indexOf("%20"));
    }

}
