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
package com.ocs.dynamo.service.impl;

import java.util.stream.Stream;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ocs.dynamo.service.UserDetailsService;

/**
 * User details service - used for retrieving the user's current roles and name
 * 
 * @author bas.rutten
 *
 */
public class DefaultUserDetailsServiceImpl implements UserDetailsService {

    private static final String SYSTEM = "system";

    @Override
    public String getCurrentUserName() {
        try {
            SecurityContext ctx = SecurityContextHolder.getContext();
            return ctx.getAuthentication().getName();
        } catch (Exception ex) {
            // ignore - no request available during integration test
            return SYSTEM;
        }
    }

    @Override
    public boolean isUserInRole(String... roles) {
        try {
            SecurityContext ctx = SecurityContextHolder.getContext();
            return ctx.getAuthentication().getAuthorities().stream()
                    .anyMatch(c -> Stream.of(roles).anyMatch(r -> c.getAuthority().equals(r)));
        } catch (Exception ex) {
            return false;
        }
    }
}
