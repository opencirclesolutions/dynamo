/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.component;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.server.StreamResource;

public class DefaultEmbeddedTest {

    @Test
    public void test() {
        DefaultEmbedded embedded = new DefaultEmbedded("Caption", new byte[] { 1, 2, 3, 4 });

        Assert.assertTrue(embedded.getSource() instanceof StreamResource);
        StreamResource resource = (StreamResource) embedded.getSource();
        Assert.assertTrue(resource.getStreamSource().getStream() instanceof ByteArrayInputStream);
    }

    @Test
    public void testEmpty() {
        DefaultEmbedded embedded = new DefaultEmbedded("Caption", null);
        Assert.assertNull(embedded.getSource());
    }
}
