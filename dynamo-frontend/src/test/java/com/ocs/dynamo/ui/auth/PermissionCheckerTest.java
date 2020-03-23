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
package com.ocs.dynamo.ui.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.test.BaseMockitoTest;
import com.ocs.dynamo.ui.auth.impl.DefaultPermissionCheckerImpl;

public class PermissionCheckerTest extends BaseMockitoTest {

    @InjectMocks
    private DefaultPermissionCheckerImpl checker = new DefaultPermissionCheckerImpl("com.ocs.dynamo");

    @Mock
    private UserDetailsService userDetailsService;

    @BeforeEach
    public void setup() {
        checker.postConstruct();
    }

    @Test
    public void testFindViews() {

        List<String> viewNames = checker.getViewNames();
        assertEquals(2, viewNames.size());
        assertTrue(viewNames.contains("TestView"));
        assertTrue(viewNames.contains("Destination 1.1"));
    }

    /**
     * Test that the "edit only" setting is correctly set
     */
    @Test
    public void testEditOnly() {
        assertTrue(checker.isEditOnly("TestView"));
        assertTrue(checker.isEditOnly("Destination 1.1"));
    }
}
