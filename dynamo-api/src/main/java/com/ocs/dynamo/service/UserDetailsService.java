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
package com.ocs.dynamo.service;

/**
 * Service for retrieving the details of the logged in user
 * 
 * @author bas.rutten
 */
public interface UserDetailsService {

    /**
     * Retrieve the name of the currently logged in user
     */
    String getCurrentUserName();

    /**
     * Returns true when the user is in (at least one of) the provided roles
     * 
     * @param roles
     * @return
     */
    boolean isUserInRole(String... roles);

}
