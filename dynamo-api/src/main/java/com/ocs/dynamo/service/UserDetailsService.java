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

import com.ocs.dynamo.domain.model.EntityModel;

/**
 * Service for retrieving the details of the logged-in user
 *
 * @author bas.rutten
 */
public interface UserDetailsService {

    /**
     * Returns true when the user is in (at least one of) the provided roles
     *
     * @param roles the collection of roles
     * @return true if the user is in at least one of the provided roles, false otherwise
     */
    boolean isUserInRole(String... roles);

    /**
     * Checks whether the user is allowed to perform read/search actions for the provided entity model
     * @param model the model to check for
     */
    void validateReadAllowed(EntityModel<?> model);

    /**
     * Checks whether the user is allowed to perform write actions for the provided entity model
     * @param model the model to check for
     */
    void validateWriteAllowed(EntityModel<?> model);

    /**
     * Checks whether the user is allowed to perform delete actions for the provided entity model
     * @param model the model to check for
     */
    void validateDeleteAllowed(EntityModel<?> model);

}
